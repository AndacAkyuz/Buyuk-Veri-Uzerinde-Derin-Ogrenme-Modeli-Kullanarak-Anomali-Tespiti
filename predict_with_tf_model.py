import sys
import pandas as pd
from tensorflow.keras.models import load_model

# Komut satırı argümanlarından model yolu ve veri yolu alın
model_path = sys.argv[1]
data_path = sys.argv[2]

# Modeli yükleme
model = load_model(model_path)

# Veriyi yükleme
data = pd.read_csv(data_path)
X = data.drop(columns=["class"])

# Tahmin yapma
predictions = model.predict(X)
data["predictions"] = predictions

# Tahmin sonuçlarını CSV'ye kaydetme
output_path = "predictions.csv"
data.to_csv(output_path, index=False)
print(f"Tahminler kaydedildi: {output_path}")
