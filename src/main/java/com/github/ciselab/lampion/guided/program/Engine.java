package com.github.ciselab.lampion.guided.program;

import com.github.ciselab.lampion.core.program.EngineResult;
import com.github.ciselab.lampion.core.transformations.*;
import com.github.ciselab.lampion.core.transformations.transformers.RemoveAllCommentsTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spoon.SpoonException;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * This is the engine for the transformations used in this project.
 * This class is copied from the Lampion project (https://github.com/ciselab/Lampion).
 * The only thing adjusted is that each transformer is used when the number of transformations allow it instead of having an distribution with
 * equal probability for each transformer.
 */
public class Engine {
    private static Logger logger = LogManager.getLogger(Engine.class);

    // Used to instantiate the random seeds of the delegated Transformers in the default TransformerRegistry
    public static long globalRandomSeed = 2020;

    Random random = new Random(globalRandomSeed);

    Path codeDirectory;
    Path outputDirectory;
    TransformerRegistry registry;

    // The scope by which to quantify the number of transformations, "setNumberOfTransformationsPerScope" for more info
    public enum TransformationScope {
        global,            // "X Transformations in total, anywhere, evenly distributed throughout all files"
        perMethod,         // "X Transformations per Method found, but evenly distributed throughout all files"
        perClass,          // "X Transformations per Class found, but evenly distributed throughout all files"
        perClassEach,      // "X Transformations per Class"
        perMethodEach      // "X Transformations per Method"
    }

    long numberOfTransformationsPerScope = 100;
    com.github.ciselab.lampion.core.program.Engine.TransformationScope scope = com.github.ciselab.lampion.core.program.Engine.TransformationScope.global;

    // These are helpers for "perClassEach" and "perMethodEach"
    // To iterate over the classes and methods until there are no more transformations
    private int classIndex = 0;
    private List<CtClass> classes = new ArrayList<>();
    private int methodIndex = 0;
    private List<CtMethod> methods = new ArrayList<>();

    private boolean removeAllComments = false; // Whether or not to remove all comments before printing

    private boolean writeJavaOutput = true; // This switch enables/disables pretty printing of altered java files

    public Engine(String codeDirectory, String outputDirectory, TransformerRegistry registry) {
        // Sanity Checks
        if (codeDirectory == null || codeDirectory.isEmpty() || codeDirectory.isBlank()) {
            throw new UnsupportedOperationException("Code Directory cannot be null or empty");
        }
        if (outputDirectory == null || outputDirectory.isEmpty() || outputDirectory.isBlank()) {
            throw new UnsupportedOperationException("Output Directory cannot be null or empty");
        }
        if (registry == null) {
            throw new UnsupportedOperationException("Registry cannot be null");
        }
        if (registry.getRegisteredTransformers().size() == 0) {
            logger.warn("Received Registry " + registry.name + " without any registered transformers.");
        }
        // Setting fields
        this.codeDirectory = Path.of(codeDirectory);
        this.outputDirectory = Path.of(outputDirectory);
        this.registry = registry;
    }

    public EngineResult run(CtModel codeRoot) {
        logger.info("Starting Engine with Registry " + registry.name + "[" + registry.getRegisteredTransformers().size()
                + " transformers] reading from " + codeDirectory + " writing to " + outputDirectory);

        Instant startOfEngine = Instant.now();

        EngineResult.Builder builder = new EngineResult.Builder(codeRoot, codeDirectory.toAbsolutePath().toString(), outputDirectory.toAbsolutePath().toString(), registry)
                .javaOutput(writeJavaOutput)
                .randomSeed(random);

        // Note:
        // It is important that methods are instantiated here and not while transformations are running,
        // as maybe there are additional Methods created. This way, only ur-elements will be altered.
        classes = codeRoot.getElements(c -> c instanceof CtClass);
        methods = codeRoot.getElements(c -> c instanceof CtMethod);

        logger.debug("Found " + classes.size() + " Classes and "
                + codeRoot.getElements(f -> f instanceof CtMethod).size() + " methods at " + codeDirectory);
        if (classes.size() == 0 || methods.size() == 0) {
            logger.error("Either found no classes or no methods - exiting early. " +
                    "Check your configuration, whether it points to actual files.");
            return builder.build();
        }

        // Step 2:
        // Apply the Transformations according to distribution
        List<TransformationResult> results = new ArrayList<>();
        // Step 2.1:
        // set the total number of transformations regarding the scope
        long totalTransformationsToDo = switch (scope) {
            case global -> numberOfTransformationsPerScope;
            case perMethod ->
                    numberOfTransformationsPerScope * codeRoot.filterChildren(c -> c instanceof CtMethod).list().size();
            case perClass ->
                    numberOfTransformationsPerScope * codeRoot.filterChildren(c -> c instanceof CtClass).list().size();
            case perMethodEach ->
                    numberOfTransformationsPerScope * codeRoot.filterChildren(c -> c instanceof CtMethod).list().size();
            case perClassEach ->
                    numberOfTransformationsPerScope * codeRoot.filterChildren(c -> c instanceof CtClass).list().size();
            default -> 0;
        };
        logger.debug("Applying " + totalTransformationsToDo + " Transformations evenly distributed amongst all classes");

        // Step 2.2:
        // For every to-be-applied transformation
        // Pick the next (random) element
        // Pick a random transformer
        // apply the transformer and add the result to the aggregation
        long transformationFailures = 0;
        int currentTransformer = 0;
        for (int a = 0; a < totalTransformationsToDo; a++) {
            try {
                CtElement toAlter = getNextCtElement();

                Transformer transformer = registry.getRegisteredTransformers().get(currentTransformer);

                TransformationResult result = transformer.applyAtRandom(toAlter);
                results.add(result);

                if (result != null && !result.equals(new EmptyTransformationResult())) {
                    logger.trace("Successfully applied " + result.getTransformationName() +
                            " to Element(Hash):" + result.getTransformedElement().toString().hashCode());
                } else if (result != null && result.equals(new EmptyTransformationResult())) {
                    logger.debug(transformer + " was not successfully applied.");
                }
                if (currentTransformer < registry.getRegisteredTransformers().size() - 1)
                    currentTransformer++;
                else
                    currentTransformer = 0;
            } catch (SpoonException | NullPointerException spoonException) {
                transformationFailures++;
            }
        }
        // Step 2.3:
        // Repair parent relationships which may have broken
        // classes.stream().forEach(c -> c.updateAllParentsBelow());

        Instant endOfTransformations = Instant.now();
        logger.info(String.format("Applying %d Transformations took %d ",results.size(),Duration.between(startOfEngine, endOfTransformations).toSeconds()));
        if (results.stream().filter(u -> u.equals(new EmptyTransformationResult())).count() > 0) {
            logger.debug("Of the " + results.size() + " Transformations applied, "
                    + results.stream().filter(u -> u.equals(new EmptyTransformationResult())).count() + " where malformed");
            logger.debug(transformationFailures + " transformations produced (Spoon-)errors");
        }
        // Step 2.4:
        // If enabled, remove all comments (by setting them invisible)
        if (removeAllComments) {
            RemoveAllCommentsTransformer commentRemover = new RemoveAllCommentsTransformer();
            // The Comment-Remover will inherit all compilation problems remaining - hence it does not try to compile
            // But it is not the comment-remover's fault, this would have to be fixed somewhere else
            commentRemover.setTryingToCompile(false);
            try {
                for (var c : classes) {
                    TransformationResult removeCommentResult = commentRemover.applyAtRandom(c);
                    results.add(removeCommentResult);
                    logger.debug("Removed all Comments from the Java Output files");
                }
            } catch (SpoonException spoonException) {
                logger.error("Received a SpoonException while removing comments", spoonException);
            }
        }
        classes.forEach(c -> c.updateAllParentsBelow());

        builder.totalTransformations(totalTransformationsToDo)
                .transformationFailures(transformationFailures)
                .transformationResults(results);

        return builder.build();
    }

    /**
     * Getter for the code Directory field.
     *
     * @return the code directory.
     */
    public String getCodeDirectory() {
        return codeDirectory.toAbsolutePath().toString();
    }

    /**
     * Looks in the initially found classes/methods for the next specified element according to specified scope.
     * Classes and Methods are returned in a order dependent from structure of the program,
     * but are always in the same order (for multiple iterations).
     * <p>
     * Heavily relies on object-level index counters and methods. If there are issues, look there first.
     * The object-level indices are also handled in this method.
     *
     * @return the next element to alter, according to scope.
     */
    private CtElement getNextCtElement() {
        CtElement toAlter = null;
        switch (scope) {
            // For these, just pick random classes and methods
            case global, perMethod, perClass:
                toAlter = classes.get(random.nextInt(classes.size()));
                break;
            case perMethodEach: {
                // Pick next method
                toAlter = methods.get(methodIndex);
                // Move or reset methodIndex
                methodIndex = methodIndex < methods.size() - 1 ? methodIndex + 1 : 0;
            }
            break;
            case perClassEach: {
                // Pick (specific) next class
                toAlter = classes.get(classIndex);
                // Move or reset classIndex
                classIndex = classIndex < classes.size() - 1 ? classIndex + 1 : 0;
            }
            break;
            default:
                logger.error("Found unknown/unhandled Scope in Engine");
        }
        return toAlter;
    }

    /**
     * This method sets the number of transformations and its scope.
     * They are multiplied with the item under alternation, example:
     * The system has 2 classes, scope classes and transformations 100 -> 200 Transformations in total.
     * The system has 10 methods, scope method, and transformations 10 -> 100 Transformations in total.
     * <p>
     * The transformations are (currently) applied evenly among all items, so setting the scope to
     * "perMethod" does not mean that every method is altered 10 times,
     * but in average there will be 10 alternations per method.
     *
     * @param transformations
     * @param scope
     * @throws UnsupportedOperationException for negative number of transformations
     */
    public void setNumberOfTransformationsPerScope(long transformations, com.github.ciselab.lampion.core.program.Engine.TransformationScope scope) {
        if (transformations < 0) {
            throw new UnsupportedOperationException("Number of transformations cannot be negative");
        } else if (transformations == 0) {
            logger.info("Number of transformations is set to 0. This might not be desired.");
        }

        this.scope = scope;
        this.numberOfTransformationsPerScope = transformations;
    }

    /**
     * Whether to write to the output folder or not.
     * All other logic is still applied as usual.
     * <p>
     * This is mostly intended to make tests easier without the need for file cleanup.
     *
     * @param val true if you want to write output, false otherwise.
     */
    public void setWriteJavaOutput(boolean val) {
        writeJavaOutput = val;
    }

    /**
     * This method sets whether all comments are removed or not.
     * The comments are still entities in the AST, but are not in the toString() or prettyprinting.
     *
     * @param val whether or not to remove all comments in all files - true for removal, false for keeping
     */
    public void setRemoveAllComments(boolean val) {
        this.removeAllComments = val;
    }

    /**
     * Sets the random number provider to using a certain seed.
     * Used for testing and repeatable experiments.
     * The default seed at initialization is the seed provided in App
     *
     * @param seed
     */
    public void setRandomSeed(long seed) {
        this.random = new Random(seed);
    }
}
