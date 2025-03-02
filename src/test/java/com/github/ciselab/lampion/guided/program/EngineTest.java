package com.github.ciselab.lampion.guided.program;

import com.github.ciselab.lampion.core.program.EngineResult;
import com.github.ciselab.lampion.core.transformations.TransformationResult;
import com.github.ciselab.lampion.core.transformations.Transformer;
import com.github.ciselab.lampion.core.transformations.TransformerRegistry;
import com.github.ciselab.lampion.core.transformations.transformers.IfFalseElseTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.IfTrueTransformer;
import com.github.ciselab.lampion.guided.program.Engine;
import org.junit.jupiter.api.*;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the Engine. Most tests have been copied from the Lampion project (https://github.com/ciselab/Lampion).
 * We test whether the functionality that was specified in the Lampion project still works and whether the new implementation of the distribution
 * also works as desired.
 */
public class EngineTest {

    private static String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_simple";
    private static String outputTestFolder = "./src/test/resources/engine_spooned/";

    @BeforeAll
    @AfterAll
    static void folder_cleanup() throws IOException {
        if(Files.exists(Paths.get(outputTestFolder))) {
            Files.walk(Paths.get(outputTestFolder))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @BeforeEach
    void createOutputFolderIfNotExists() throws IOException {
        if(!Files.exists(Paths.get(outputTestFolder))){
            Files.createDirectory(Paths.get(outputTestFolder));
        }
    }

    @Test
    void testSetTransformationScope_negativeNumberOfTransformations_shouldThrowException(){
        TransformerRegistry registry = new TransformerRegistry("Test");

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        assertThrows(UnsupportedOperationException.class,
                () -> testObject.setNumberOfTransformationsPerScope(-1, com.github.ciselab.lampion.core.program.Engine.TransformationScope.global));
    }

    @Test
    void testSetTransformationScope_allGood_shouldBeSet(){
        TransformerRegistry registry = new TransformerRegistry("Test");

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setNumberOfTransformationsPerScope(5, com.github.ciselab.lampion.core.program.Engine.TransformationScope.global);
        return;
    }

    @Test
    void testGetFinishedResults_ZeroTransformations_ShouldGiveEmptyList() {
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_perMethodEach";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setNumberOfTransformationsPerScope(0, com.github.ciselab.lampion.core.program.Engine.TransformationScope.perClassEach);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        assertTrue(result.getTransformationResults().isEmpty());
    }

    @Test
    void testGetFinishedResults_RunEngine_ShouldNotGiveEmptyList(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_perMethodEach";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setWriteJavaOutput(false);

        testObject.setNumberOfTransformationsPerScope(5,
                com.github.ciselab.lampion.core.program.Engine.TransformationScope.global);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        assertFalse(result.getTransformationResults().isEmpty());
    }

    @Tag("System")
    @Tag("File")
    @RepeatedTest(3)
    void testPerClassEachScope_ShouldApplyEvenlyToMethods(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_perMethodEach";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setWriteJavaOutput(false);

        testObject.setNumberOfTransformationsPerScope(5, com.github.ciselab.lampion.core.program.Engine.TransformationScope.perClassEach);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        // Easy Check on Size
        assertEquals(10,result.getTransformationResults().size());

        // Distribution Checks
        result.getTransformationResults().stream()
                .collect(Collectors.groupingBy(t -> ((CtClass)t.getTransformedElement().getParent(p -> p instanceof CtClass)).getSimpleName()))
                .values().stream().mapToLong(u -> u.size())
                .forEach(f -> assertEquals(5,f));
        assertEquals(2,result.getTransformationResults().stream()
                .collect(Collectors.groupingBy(t -> ((CtClass)t.getTransformedElement().getParent(p -> p instanceof CtClass)).getSimpleName())).entrySet().size());
    }

    @Tag("System")
    @Tag("File")
    @RepeatedTest(3)
    void testPerClassScope_ShouldApplyEvenlyToMethods(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_perMethodEach";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setWriteJavaOutput(false);

        testObject.setNumberOfTransformationsPerScope(5, com.github.ciselab.lampion.core.program.Engine.TransformationScope.perClass);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        // Easy Check on Size
        assertEquals(10,result.getTransformationResults().size());
    }

    @Tag("System")
    @Tag("File")
    @RepeatedTest(3)
    void testPerMethodEachScope_ShouldApplyEvenlyToMethods(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_perMethodEach";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setWriteJavaOutput(false);

        testObject.setNumberOfTransformationsPerScope(3, com.github.ciselab.lampion.core.program.Engine.TransformationScope.perMethodEach);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        // Easy Check on Size
        assertEquals(12,result.getTransformationResults().size());
        // Distribution Checks
        result.getTransformationResults().stream()
                .collect(Collectors.groupingBy(t -> ((CtMethod)t.getTransformedElement()).getSimpleName()))
                .values().stream().mapToLong(u -> u.size())
                .forEach(f -> assertEquals(3,f));
        assertEquals(4,result.getTransformationResults().stream()
                .collect(Collectors.groupingBy(t -> ((CtMethod)t.getTransformedElement()).getSimpleName())).entrySet().size());
    }

    @Tag("System")
    @Tag("File")
    @RepeatedTest(3)
    void testPerMethodScope_ShouldApplyMultipleOfMethods(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_perMethodEach";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setWriteJavaOutput(false);

        testObject.setNumberOfTransformationsPerScope(3, com.github.ciselab.lampion.core.program.Engine.TransformationScope.perMethod);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        // Easy Check on Size
        assertEquals(12,result.getTransformationResults().size());
    }

    @Tag("System")
    @Tag("File")
    @Test
    void testEngineRun_withDeleteComments_ShouldDeleteJavaDocs(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_with_comments";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        // Do just little transformations to be faster
        testObject.setNumberOfTransformationsPerScope(3,
                com.github.ciselab.lampion.core.program.Engine.TransformationScope.global);

        testObject.setRemoveAllComments(true);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        result.getTransformationResults().forEach(
                p -> assertFalse(p.getTransformedElement().toString().contains("Comment") ||p.getTransformedElement().toString().contains("JavaDoc"))
        );
    }

    @Tag("System")
    @Tag("File")
    @Test
    void testEngineRun_sameResultWithMultipleRuns() {
        String pathToTestFileFolder = "./src/test/resources/input_test/test";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer(1));
        registry.registerTransformer(new IfTrueTransformer(2));

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setWriteJavaOutput(false);

        testObject.setNumberOfTransformationsPerScope(3, com.github.ciselab.lampion.core.program.Engine.TransformationScope.perMethod);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult baseResult = testObject.run(codeRoot);
        for(int i = 0; i < 5; i++) {
            EngineResult newResult = testObject.run(codeRoot);
            assertEquals(baseResult.getCodeRoot(),
                    newResult.getCodeRoot());
        }
    }

    @Tag("System")
    @Tag("File")
    @Test
    void testEngineRun_withDeleteComments_ShouldDeleteInlineComments(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_with_comments";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        // Do just little transformations to be faster
        testObject.setNumberOfTransformationsPerScope(3, com.github.ciselab.lampion.core.program.Engine.TransformationScope.perClassEach);

        testObject.setRemoveAllComments(true);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        result.getTransformationResults().forEach(
                p -> assertFalse(p.getTransformedElement().toString().contains("Comment") ||p.getTransformedElement().toString().contains("JavaDoc"))
        );
    }

    @Tag("System")
    @Tag("File")
    @Test
    void testEngineRun_withDeleteComments_ShouldDeleteBlockComments(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_with_comments";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);
        // Do just little transformations to be faster
        testObject.setNumberOfTransformationsPerScope(3, com.github.ciselab.lampion.core.program.Engine.TransformationScope.perClassEach);

        testObject.setRemoveAllComments(true);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        for(TransformationResult p : result.getTransformationResults()){
            assertFalse(p.getTransformedElement().toString().contains("Comment") ||p.getTransformedElement().toString().contains("JavaDoc"));
        }
    }

    @Tag("System")
    @Tag("File")
    @Test
    void testEngineRun_withDeleteComments_SingleTrans_ShouldDeleteBlockComments(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_with_comments";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        // Do just little transformations to be faster
        testObject.setNumberOfTransformationsPerScope(1, com.github.ciselab.lampion.core.program.Engine.TransformationScope.global);

        testObject.setRemoveAllComments(true);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        for(TransformationResult p : result.getTransformationResults()){
            assertFalse(p.getTransformedElement().toString().contains("Comment") ||p.getTransformedElement().toString().contains("JavaDoc"));
        }
    }

    @Tag("System")
    @Tag("File")
    @Tag("Regression")
    @Test
    void testRemoveComments_doNotRemoveComments_commentsShouldBeKept(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_with_comments";
        TransformerRegistry registry = new TransformerRegistry("Test");

        Transformer ifFalse = new IfFalseElseTransformer(2);
        registry.registerTransformer(ifFalse);

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        // Do just little transformations to be faster
        testObject.setNumberOfTransformationsPerScope(4, com.github.ciselab.lampion.core.program.Engine.TransformationScope.global);

        testObject.setRemoveAllComments(false);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        for(TransformationResult p : result.getTransformationResults()){
            assertTrue(p.getTransformedElement().toString().contains("Comment") ||p.getTransformedElement().toString().contains("JavaDoc"));
        }
    }

    @Tag("System")
    @Tag("File")
    @Test
    void testEngineRun_withDeleteComments_ZeroTrans_ShouldDeleteBlockComments(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_with_comments";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        // Do just little transformations to be faster
        testObject.setNumberOfTransformationsPerScope(0, com.github.ciselab.lampion.core.program.Engine.TransformationScope.global);

        testObject.setRemoveAllComments(true);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        for(TransformationResult p : result.getTransformationResults()){
            assertFalse(p.getTransformedElement().toString().contains("Comment"));
        }
    }

    @Tag("System")
    @Tag("File")
    @Tag("Regression")
    @Test
    void testEngineRun_withDeleteComments_ShouldHaveRemovalInManifest(){
        // At the beginning, the comment-removal-transformations were outside of the manifest logic
        // After 1.2 they are run separate (after transformations) but also added to the manifest
        // They were often / usually run, but they threw an error while somewhat still working

        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_with_comments";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setNumberOfTransformationsPerScope(5, com.github.ciselab.lampion.core.program.Engine.TransformationScope.global);

        testObject.setRemoveAllComments(true);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        boolean haveSeenRemoveComments = false;
        for(TransformationResult p : result.getTransformationResults()){
            haveSeenRemoveComments = haveSeenRemoveComments || p.getTransformationName().equals("RemoveAllComments");
        }
        assertTrue(haveSeenRemoveComments);
    }


    @Tag("System")
    @Tag("File")
    @Tag("Regression")
    @Test
    void testEngineRun_withDeleteComments_noTransformations_ShouldHaveRemovalInManifest(){
        // At the beginning, the comment-removal-transformations were outside of the manifest logic
        // After 1.2 they are run separate (after transformations) but also added to the manifest
        // They were often / usually run, but they threw an error while somewhat still working

        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_with_comments";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setNumberOfTransformationsPerScope(0, com.github.ciselab.lampion.core.program.Engine.TransformationScope.global);

        testObject.setRemoveAllComments(true);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        boolean haveSeenRemoveComments = false;
        for(TransformationResult p : result.getTransformationResults()){
            haveSeenRemoveComments = haveSeenRemoveComments || p.getTransformationName().equals("RemoveAllComments");
        }
        assertTrue(haveSeenRemoveComments);
    }

    @Tag("System")
    @Tag("File")
    @Test
    void testEngineRun_withoutDeleteComments_ShouldHaveNoRemovalInManifest(){
        // At the beginning, the comment-removal-transformations were outside of the manifest logic
        // After 1.2 they are run separate (after transformations) but also added to the manifest
        // This is the negative test, checking that without removal they are not in the manifest

        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_with_comments";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setNumberOfTransformationsPerScope(5, com.github.ciselab.lampion.core.program.Engine.TransformationScope.global);

        testObject.setRemoveAllComments(false);

        Launcher launcher = new spoon.Launcher();
        launcher.addInputResource(testObject.getCodeDirectory());
        CtModel codeRoot = launcher.buildModel();
        launcher.getFactory().getEnvironment().setAutoImports(false);
        EngineResult result = testObject.run(codeRoot);

        boolean haveSeenRemoveComments = false;
        for(TransformationResult p : result.getTransformationResults()){
            haveSeenRemoveComments = haveSeenRemoveComments || p.getTransformationName().equals("RemoveAllComments");
        }
        assertFalse(haveSeenRemoveComments);
    }

    @Test
    void testConstructor_NullCodeDirectory_shouldThrowException(){
        TransformerRegistry registry = new TransformerRegistry("Test");

        assertThrows(UnsupportedOperationException.class, () ->  new Engine(null,outputTestFolder,registry));
    }

    @Test
    void testConstructor_EmptyCodeDirectory_shouldThrowException(){
        TransformerRegistry registry = new TransformerRegistry("Test");

        assertThrows(UnsupportedOperationException.class, () ->  new Engine("",outputTestFolder,registry));
    }

    @Test
    void testConstructor_BlankCodeDirectory_shouldThrowException(){
        TransformerRegistry registry = new TransformerRegistry("Test");

        assertThrows(UnsupportedOperationException.class, () ->  new Engine("  \n",outputTestFolder,registry));
    }

    @Test
    void testConstructor_NullOutDirectory_shouldThrowException(){
        TransformerRegistry registry = new TransformerRegistry("Test");

        assertThrows(UnsupportedOperationException.class, () ->  new Engine(pathToTestFileFolder,null,registry));
    }

    @Test
    void testConstructor_EmptyOutDirectory_shouldThrowException(){
        TransformerRegistry registry = new TransformerRegistry("Test");

        assertThrows(UnsupportedOperationException.class, () ->  new Engine(pathToTestFileFolder,"",registry));
    }

    @Test
    void testConstructor_BlankOutDirectory_shouldThrowException(){
        TransformerRegistry registry = new TransformerRegistry("Test");

        assertThrows(UnsupportedOperationException.class, () ->  new Engine(pathToTestFileFolder,"  \n",registry));
    }

    @Test
    void testSetSeed_ChangesSeed(){
        String pathToTestFileFolder = "./src/test/resources/javafiles/javafiles_with_comments";
        TransformerRegistry registry = new TransformerRegistry("Test");
        registry.registerTransformer(new IfFalseElseTransformer());

        Engine testObject = new Engine(pathToTestFileFolder,outputTestFolder,registry);

        testObject.setRandomSeed(250);
    }

    @Test
    void testConstructor_NullRegistry_shouldThrowException(){
        assertThrows(UnsupportedOperationException.class, () ->  new Engine(pathToTestFileFolder,outputTestFolder,null));
    }
}
