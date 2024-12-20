import pandas as pd
import tensorflow as tf
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import confusion_matrix, classification_report, roc_curve, auc
from imblearn.over_sampling import SMOTE
import matplotlib.pyplot as plt
import numpy as np

# Veri setini yükleme
file_path = "annthyroid_21feat_normalised.csv"
data = pd.read_csv(file_path)

# Özellikler ve hedef değişken
X = data.drop(columns=["class"])
y = data["class"]

# SMOTE ile veri artırma
smote = SMOTE(random_state=1234)
X_resampled, y_resampled = smote.fit_resample(X, y)

# Dengelenmiş veri seti sınıf dağılımı
print("Sınıf dağılımı SMOTE sonrası:")
print(pd.Series(y_resampled).value_counts())

# Eğitim ve test verilerini ayırma
X_train, X_test, y_train, y_test = train_test_split(
    X_resampled, y_resampled, test_size=0.2, random_state=1234
)

# Veriyi standartlaştırma
scaler = StandardScaler()
X_train = scaler.fit_transform(X_train)
X_test = scaler.transform(X_test)

# LSTM için veriyi 3 boyutlu hale getirme
X_train = X_train.reshape((X_train.shape[0], X_train.shape[1], 1))
X_test = X_test.reshape((X_test.shape[0], X_test.shape[1], 1))

# LSTM modelini oluşturma
model = tf.keras.Sequential([
    tf.keras.layers.LSTM(128, activation='tanh', return_sequences=True, input_shape=(X_train.shape[1], 1)),
    tf.keras.layers.Dropout(0.3),
    tf.keras.layers.LSTM(64, activation='tanh', return_sequences=False),
    tf.keras.layers.Dropout(0.3),
    tf.keras.layers.Dense(32, activation='relu'),
    tf.keras.layers.Dense(1, activation='sigmoid')
])

# Modeli derleme
model.compile(optimizer='adam', 
              loss='binary_crossentropy', 
              metrics=['accuracy'])

# Modeli eğitme
history = model.fit(X_train, y_train, epochs=30, batch_size=32, validation_split=0.2)

# Modeli değerlendirme
loss, accuracy = model.evaluate(X_test, y_test)
print(f"Test Loss: {loss}")
print(f"Test Accuracy: {accuracy}")

# Tahmin yapma
predictions = model.predict(X_test)
threshold = 0.5
predictions_binary = (predictions > threshold).astype(int)

# Performans metrikleri
print("Classification Report:")
print(classification_report(y_test, predictions_binary))

# Confusion Matrix
conf_matrix = confusion_matrix(y_test, predictions_binary)
print("Confusion Matrix:")
print(conf_matrix)

# ROC ve AUC
fpr, tpr, _ = roc_curve(y_test, predictions)
roc_auc = auc(fpr, tpr)
plt.figure()
plt.plot(fpr, tpr, color='blue', label=f'ROC Curve (area = {roc_auc:.2f})')
plt.xlabel('False Positive Rate')
plt.ylabel('True Positive Rate')
plt.title('Receiver Operating Characteristic')
plt.legend(loc='lower right')
plt.show()

# Eğitim ve doğrulama kaybı/grafik çizimi
plt.figure()
plt.plot(history.history['loss'], label='Training Loss')
plt.plot(history.history['val_loss'], label='Validation Loss')
plt.xlabel('Epochs')
plt.ylabel('Loss')
plt.title('Loss Over Epochs')
plt.legend()
plt.show()

plt.figure()
plt.plot(history.history['accuracy'], label='Training Accuracy')
plt.plot(history.history['val_accuracy'], label='Validation Accuracy')
plt.xlabel('Epochs')
plt.ylabel('Accuracy')
plt.title('Accuracy Over Epochs')
plt.legend()
plt.show()

# Modeli kaydetme
model_save_path = "lstm_model_smote.h5"
model.save(model_save_path)
print(f"Model başarıyla kaydedildi: {model_save_path}")


# Test veri setinden 5 rastgele örnek seçme
random_indices = np.random.choice(X_test.shape[0], 5, replace=False)
sample_data = X_test[random_indices]
sample_true_labels = y_test.iloc[random_indices]

# Tahmin yapma
sample_predictions = model.predict(sample_data)
sample_predictions_binary = (sample_predictions > threshold).astype(int)

# Sonuçları gösterme
print("Gerçek Etiketler:")
print(sample_true_labels.values)
print("Tahmin Edilen Etiketler:")
print(sample_predictions_binary.flatten())
print("Tahminlerin Olasılıkları:")
print(sample_predictions.flatten())