import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# Load the dataset
file_path = 'annthyroid_21feat_normalised.csv'
dataset = pd.read_csv(file_path)

# Configure visualization style
sns.set(style="whitegrid")

# Plot histogram for all features
all_columns = dataset.columns
dataset[all_columns].hist(bins=20, figsize=(20, 15), layout=(5, 5), color='blue', alpha=0.7)
plt.suptitle("Histogram of All Features", fontsize=16)
plt.tight_layout(rect=[0, 0, 1, 0.95])
plt.show()

# Boxplot for all features to check for outliers
plt.figure(figsize=(20, 10))
sns.boxplot(data=dataset[all_columns], palette="coolwarm")
plt.title("Boxplot of All Features", fontsize=16)
plt.xticks(rotation=90)
plt.show()

# Correlation heatmap for all numerical features
plt.figure(figsize=(15, 12))
correlation_matrix = dataset.corr()
sns.heatmap(correlation_matrix, annot=True, fmt=".2f", cmap="coolwarm", cbar=True)
plt.title("Correlation Heatmap of All Features", fontsize=16)
plt.show()

# Class distribution
plt.figure(figsize=(8, 6))
sns.countplot(x='class', data=dataset, palette="viridis")
plt.title("Class Distribution", fontsize=16)
plt.xlabel("Class")
plt.ylabel("Count")
plt.show()

# Pairplot for selected features to analyze relationships
selected_features = all_columns[:6]  # Selecting the first six columns for pairplot
sns.pairplot(dataset[selected_features], diag_kind="kde", markers="o", palette="husl")
plt.suptitle("Pairplot of Selected Features", y=1.02, fontsize=16)
plt.show()

