# JobHunter - Moroccan Job Market Analysis Tool

JobHunter is a sophisticated Java-based application designed to analyze the Moroccan job market through web scraping, data processing, and advanced analytics. It provides comprehensive insights into job opportunities, market trends, and career analytics through an intuitive graphical interface.

## Core Features

### 1. Multi-Source Job Scraping
- Automated scraping from major Moroccan job portals:
  - Rekrute
  - Anapec
  - EmploiMa
  - KhdmaMa
  - MarocAnnonces
  - MonCallCenter
  - StagairesMa
- Two-phase scraping architecture:
  - Links collection (LinksScraper): Efficiently gathers job posting URLs
  - Detailed data extraction (DataScraper): Extracts comprehensive job information

### 2. Advanced Data Processing Pipeline
- Dual cleaning approach for optimal data quality:
  1. AI-Powered Cleaning (Cleaner.java):
     - Uses OpenRouter API for intelligent data structuring and cleaning using GPT-4o-mini.
     - Extracts and normalizes key job information
     - Handles multilingual content (French/Arabic/English)
     - Standardizes data format and encoding
  2. RegEx-Based Cleaning (RegExCleaner.java):
     - Site-specific cleaners for each job portal
     - Pattern-based data extraction
     - Dictionary-supported field normalization
     - Robust error handling and validation
     - Uses `java.text.Normalizer` class for text normalization

### 3. Comprehensive Database Integration
- Structured MySQL database schema
- Rich job information storage including:
  - Job details (title, description, requirements...)
  - Company information
  - Location and sector data
  - Salary and experience requirements
  - Skills (technical and soft)
  - Educational requirements
  - Contract details
  - Language requirements
  - Application deadlines


### 4. Modern GUI Interface
The application features a user-friendly interface with five main sections:

1. **Database Management (refreshDB page)**
   - Manual database updates
   - lets you choose which pipeline to use for data cleaning
   - Progress tracking for scraping operations
   - Data validation and error reporting
   - database export

2. **Job Browser (browse jobs page)**
   - Advanced search and filtering capabilities
   - Detailed job view with formatted information

3. **AI Chatbot**
   - Interactive job market analysis
   - Personalized job recommendations
   - Market trend insights
   - Career guidance support

4. **Statistics Dashboard**
   - Geographical distribution analysis
   - Salary range analytics
   - Company and sector insights
   - Contract type distribution

5. **ML Models**
   - Salary prediction based on job parameters, uses Linear Regression.
   - Sector prediction model based on job patrameters, uses Decision Tree (J48).
   - Job Recommendation based on job parameters, uses KNN.

## Technical Implementation

### Architecture
- **Java 11+ Backend**
  - Maven for dependency management
  - Modular design with clear separation of concerns
  - Robust error handling and logging
  - Unit Testing with JUNIT.

### Key Components
1. **Web Scraping Module**
   - JSoup for HTML parsing
   - Selenium WebDriver for dynamic content scraping
   - Custom scraper implementations for each portal

2. **Data Processing**
   - OpenRouter API integration
   - Regular expression patterns
   - JSON-based dictionaries
   - Custom cleaning algorithms
   - The ``java.text.normalizer` for text normalization

3. **Database Layer**
   - MySQL database
   - Efficient batch processing
   - Transaction management
   - Connection pooling

4. **User Interface**
   - Swing-based GUI
   - Modern design patterns
   - Responsive layouts
   - Event-driven architecture

### Libraries and Dependencies
- JSoup for web scraping
- Selenium for dynamic content
- MySQL Connector for database operations
- JFreeChart for data visualization
- Weka for machine learning tasks
- Jackson for JSON processing
- Logback for logging

## Setup and Configuration

### Prerequisites
- Java Development Kit (JDK) 11 or higher
- MySQL Server 8.0+
- Maven 3.6+
- OpenRouter API key (Optional)

### Installation Steps
1. Clone the repository
2. Configure database connection in `DatabaseConnection.java`
3. Set up OpenRouter AI API key in `Cleaner.java`
4. Run `mvn install` to install dependencies

### Running the Application
1. Execute `mvn exec:java` to launch the GUI
2. Use the Database Management section for initial data collection
3. Navigate through different sections to explore features

## Project Structure
```
jobhunter/
├── src/main/java/com/jobhunter/
│   ├── LinksScraper/       # URL collection
│   ├── DataScraper/        # Detailed data extraction
│   ├── Cleaner/            # Data processing
│   ├── database/           # Database operations
│   ├── pages/              # GUI implementation
│   └── util/               # Utility classes
├── src/main/resources/
│   ├── dictionary/         # Data normalization files
│   └── ML/                 # Machine learning models
└── src/test/              # Test cases
```