# LungRadar 📱🫁
**AI-powered Lung Cancer Detection from Chest X-ray & CT Scan Images**

LungRadar is an Android mobile application designed to assist users in analyzing lung medical images (X-rays and CT scans) using a deep learning model to predict the presence of lung cancer. It offers a simple interface for uploading images, running predictions, and receiving actionable feedback and educational resources.

---

## 🚀 Features

- 📷 **Camera & Gallery Support**  
  Capture lung images directly or select from your gallery for analysis.

- 🧠 **AI-Powered Classification**  
  Uses a trained convolutional neural network (CNN) to identify:
    - Normal (No cancer)
    - Adenocarcinoma
    - Large-cell carcinoma
    - Squamous cell carcinoma

- 📊 **Confidence Score**  
  Displays the model’s confidence level (%) in its prediction.

- 💡 **Actionable Insights**  
  Based on results, the app shows:
    - Personalized advice
    - Risk level
    - Recommended actions (e.g., consult a doctor, maintain a healthy lifestyle)

- 📚 **Resources Section**  
  Direct links to external articles on healthy living and lung cancer screening.

---

## 🖼️ Screenshots

| Splash Screen | Upload Screen | Normal Result | Cancer Detected |
|---------------|---------------|----------------|------------------|
| 📱 Splash     | 📁 Select Image | ✅ No Cancer     | ⚠️ Cancer Detected |

---

## 📦 How it Works

1. **User uploads an image** via camera or gallery.
2. The app loads the image into a classifier powered by TensorFlow Lite.
3. The classifier predicts:
    - Whether the image is cancerous or not
    - The **type** of cancer (if present)
4. Based on prediction:
    - Redirects to `ReviewNormalActivity` or `ReviewCancerActivity`
    - Displays analysis, confidence score, and resources

---

## 📁 File Structure (Highlights)

app/
├── ui/
│ ├── AnalyseFragment.kt # Core image input + classification
│ ├── ReviewNormalActivity.kt # Normal result screen
│ ├── ReviewCancerActivity.kt # Cancer result screen
├── classifier/
│ └── LungCancerClassifier.kt # TensorFlow Lite classifier
├── res/
│ ├── layout/
│ ├── drawable/
│ ├── values/colors.xml # App-wide color palette
│ └── values/strings.xml


---

## ⚙️ Requirements

- Android Studio Hedgehog or newer
- Android Gradle Plugin 8.5+
- Android 8.0 (API 26) and above

---

## 🧠 Model Info

- Model type: CNN / TensorFlow Lite
- Input: Chest X-ray or CT scan (PNG, JPEG)
- Output: Label (`normal`, `adenocarcinoma`, `large-cell`, `squamous`) + confidence score

---

## 🛠️ Setup & Installation

1. Clone the repo:

   ```bash
   git clone https://github.com/yourusername/lungradar.git
   cd lungradar
2. Open in Android Studio

3. Connect an Android device or emulator

4. Click Run ▶

🌐 External Resources Used
- TensorFlow Lite

- Lung Cancer Screening Guidelines

- American Lung Association

🤝 Contributors
Sharon Akinyi – Developer & ML Integrator

OpenAI GPT Support – Design and coding assistance

📜 License
This project is open source and available under the MIT License.

# Lung-CancerMachine-Learning-System
