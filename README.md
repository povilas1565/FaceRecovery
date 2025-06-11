# FaceRecovery

1. Структура проекта:
FaceRecovery/
├── pom.xml 
├── src/
│   └── main/
│       ├── java/
│       │   └── com/agora/facerecovery/
│       │       ├── MainApplication.java       ← главный класс с `Application.launch(...)`
│       │       ├── controller/
│       │       │   └── MainController.java
│       │       └── model/
│       │           └── FaceResult.java
|       |           └── ResultRow.java
|       |       └── utils/
|       |           └── ExcelExporter.java
|       |   └── module-info.java
│       └── resources/
│           └── main-view.fxml
│          
├── data/
│   ├── smallSet/
│   ├── smallSetClean/
│   └── withglasses/
└── python/
└── process_gan.py
└── process_mtcnn.py
└── requirements/txt

2. Убедиться, что установлены:
🧩 Java и JavaFX:
Java 17+ (java -version)

JavaFX SDK скачан и подключен (если не используешь Maven/Gradle)

🧩 Python:
python установлен (python --version)

process_gan.py и process_mtcnn.py работают из командной строки

✅ 3. Собери и запусти (в зависимости от способа сборки):
A. IntelliJ IDEA (рекомендуется)
Открой проект в IntelliJ.

Убедись, что:

Установлен SDK Java 17+ (File → Project Structure → Project SDK)

JavaFX подключён:

Если используешь Maven — зависимости подтянутся автоматически.

Если вручную — добавь JavaFX SDK в Libraries.

B. Maven запуск через терминал
mvn clean javafx:run

4. Проверка
Когда всё верно:

Загружается интерфейс.

Кнопки работают.

Нажатие "Run" вызывает Python скрипты (gan, mtcnn) и отображает результат.

Экспорт сохраняет .csv и/или .xlsx.

## Функионал
1) Загрузка и отображение изображений: оригинал, с очками, очищенное, GAN, MTCNN.

2) Обработка через GAN и MTCNN с вызовом Python-скриптов.

3) Асинхронная обработка с многопоточностью и Platform.runLater.

4) Таблица с результатами (оценки и время).

5) Экспорт в CSV и Excel (Apache POI).

6) Агрегация и отображение ошибок, если GAN/MTCNN не сработали.

7) Кнопки управления: Старт, Пауза, Стоп, Возобновление.

8) Прогресс-бар и статус.

9) Работает как на Windows, так и на Mac (если установлен Python и зависимости).

## Дальнейшие идеи по усовершенствованию, если это необходимо будет
1) Диаграммы/графики прямо в Excel.

2) Сохранение и продолжение обработки после закрытия.

3) Автосохранение результатов.

4) Настройки путей к папкам/скриптам.

5) Настройка параметров GAN/MTCNN (через UI).

6) Архивация и логирование всех попыток.


