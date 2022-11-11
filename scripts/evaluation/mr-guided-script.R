if (!require(effsize)){ install.packages("effsize") }
if (!require(data.table)){ install.packages("data.table") }
if (!require(ggplot2)){ install.packages("ggplot2") }
if (!require(epitools)){ install.packages("epitools") }
if (!require(xtable)){install.packages("xtable")}
if (!require(dplyr)){install.packages("dplyr")}

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
        a12.f1 = vd.f1$estimate,
        a12.f1_mag = vd.f1$magnitude,
        random.mrr = median(random$MRR),
        genetic.mrr = median(genetic$MRR),
        wilcoxon.mrr = wilcox.test(random$MRR, genetic$MRR, alternative = "two.sided")$p.value,
        a12.mrr = vd.mrr$estimate,
        a12.mrr_mag=vd.mrr$magnitude
      )
      count <- count + 1
    }
  }
  table.results <- rbindlist(table.results, fill = T)
  
  #sort the rows in ascending order of n. generations
  table.results <- table.results[order(generation),]
  return(table.results)
}

results.f1 <- apply_statistical_analysis(data.points, "random-F1-min", "F1-min")
results.f1 <- results.f1 %>% mutate_if(is.numeric, round, digits=4)

print(results.f1)
print(xtable(results.f1, type = "latex"), file = "f1-stats.tex")


results.mrr <- apply_statistical_analysis(data.points, "random-MRR-min", "MRR-min")
results.mrr <- results.mrr %>% mutate_if(is.numeric, round, digits=4)

print(results.mrr)
print(xtable(results.mrr, type = "latex"), file = "mrr-stats.tex")
