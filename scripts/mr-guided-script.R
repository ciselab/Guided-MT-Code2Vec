if (!require(effsize)){ install.packages("effsize") }
if (!require(data.table)){ install.packages("data.table") }
if (!require(ggplot2)){ install.packages("ggplot2") }
if (!require(epitools)){ install.packages("epitools") }

# To run the script, un-comment the next line and specify the correct path/folder 
# where the CSV file (with all the results) is stored
# setwd("~/Path to folder")

data.points <- read.csv("results.csv")

data.points$MRR <- as.numeric(as.character(data.points$MRR))
data.points$F1 <- as.numeric(as.character(data.points$F1))
data.points$generation <- as.numeric(as.character(data.points$generation))

# Comparing F1-min vs Random
apply_statistical_analysis <- function(dataset, experiment.random, experiment.genetic) {
  table.results <- vector(mode = "list", length = length(350 * 2))
  count <- 1
  for(generation in unique(data.points$generation)){
    random <- data.points[data.points$generation==generation & data.points$algorithm=="random"  & data.points$experiment==experiment.random,]
    genetic <- data.points[data.points$generation==generation & data.points$algorithm=="genetic"  & data.points$experiment==experiment.genetic,]

    # compute Vargha-Delaney Statistics
    vd.f1 = VD.A(random$F1, genetic$F1)
    vd.mrr = VD.A(random$MRR, genetic$MRR)
    if (nrow(genetic) > 0){
      table.results[[count]] <- list(
        generation = generation,
        random.f1 = median(random$F1),
        genetic.f1 = median(genetic$F1),
        wilcoxon.f1 = wilcox.test(random$F1, genetic$F1, alternative = "two.sided")$p.value,
        a12.f1 = paste(vd.f1$estimate, " (", vd.f1$magnitude, ")", sep=""),
        random.mrr = median(random$MRR),
        genetic.mrr = median(genetic$MRR),
        wilcoxon.mrr = wilcox.test(random$MRR, genetic$MRR, alternative = "two.sided")$p.value,
        a12.mrr = paste(vd.mrr$estimate, " (", vd.mrr$magnitude, ")", sep="")
      ) 
      count <- count + 1
    }
  }
  table.results <- rbindlist(table.results, fill = T)
  
  #sort the rows in ascending order of n. generations
  table.results <- table.results[order(generation),]
  return(table.results)
}

results.f1 <- apply_statistical_analysis(data.points, "random-F1-max", "F1-max")
print(results.f1)

results.mrr <- apply_statistical_analysis(data.points, "random-MRR-max", "MRR-max")
print(results.mrr)
