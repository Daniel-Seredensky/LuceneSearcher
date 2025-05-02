import pandas as pd
import matplotlib.pyplot as plt
import os

def create_boxplot_by_type(ax, filepath, subplot_title):
    try:
        df = pd.read_csv(filepath)
            
        data_by_type = []
        type_labels = []
        
        for type_name, group in df.groupby('Type'):
            data_by_type.append(group['IndexingTime'])
            type_labels.append(type_name)
        
        ax.boxplot(data_by_type, tick_labels=type_labels, notch=True)
        ax.set_ylabel("Indexing Time (ms)")
        ax.set_title(subplot_title)
        ax.grid(axis='y', linestyle='--', alpha=0.7)
                
    except Exception as e:
        print(f"Error processing {filepath}: {e}")

csv_files = [
    "DataVisualization/CIndexNotExistsCLUpdated.csv",    "DataVisualization/CIndexNotExistsVSUpdated.csv",
    "DataVisualization/CIndexExistsCLUpdated.csv",    "DataVisualization/CIndexExistsVSUpdated.csv",
]

fig, axes = plt.subplots(2, 2, figsize=(16, 20))
axes = axes.flatten()  

for i, csv_file in enumerate(csv_files):
    if i < len(axes): 
        file_basename = os.path.basename(csv_file)
        title = file_basename.replace('.csv', '').replace('Index', ' Index')
        
        create_boxplot_by_type(axes[i], csv_file, title)


plt.tight_layout()

plt.savefig("DataVisualization/Combined_BoxplotsUpdated.png")
plt.close()

