import pandas as pd
import sys

def process_csv(input_path, output_path):
    # Veri setini yükle
    data = pd.read_csv(input_path, header=None)
    
    # Eğer değerler tek sütunda sıkışmışsa, virgüle göre ayır
    if data.shape[1] == 1:
        data = data[0].str.split(',', expand=True)
    
    # İstenen sütun etiketlerini oluştur
    column_names = [
        "Dim_0", "Dim_1=0", "Dim_2=0", "Dim_3=0", "Dim_4=0", "Dim_5=0", 
        "Dim_6=0", "Dim_7=0", "Dim_8=0", "Dim_9=0", "Dim_10=0", "Dim_11=0", 
        "Dim_12=0", "Dim_13=0", "Dim_14=0", "Dim_15=0", "Dim_16", "Dim_17", 
        "Dim_18", "Dim_19", "Dim_20", "class"
    ]
    
    # Yeni sütun etiketlerini ata
    if data.shape[1] == len(column_names):
        data.columns = column_names
    else:
        print("Hata: Veri setindeki sütun sayısı beklenenden farklı.")
        sys.exit(1)

    # İkinci satırı sil
    data = data.drop(index=1)  # İkinci satır (index 1) silinir

    # Boş ya da gereksiz satırları kaldır
    data = data[~data.isnull().all(axis=1)]  # Tüm değerleri boş olan satırları kaldır
    data = data[~(data.apply(lambda x: x.str.strip().eq('')).all(axis=1))]  # Sadece boş stringlerden oluşan satırları kaldır

    # "value,,,,,,,,,,,,,,,,,,," satırını özel olarak kaldır
    data = data[~data.apply(lambda x: x.astype(str).str.contains('^value,*$', regex=True).any(), axis=1)]

    # İşlenmiş CSV'yi kaydet
    data.to_csv(output_path, index=False)
    print(f"CSV dosyası işlendi ve kaydedildi: {output_path}")

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Kullanım: python csv_processing.py <input_csv> <output_csv>")
        sys.exit(1)
    
    input_csv = sys.argv[1]
    output_csv = sys.argv[2]
    process_csv(input_csv, output_csv)