# JobHunter - Moroccan Job Market Analysis Tool

JobHunter is a sophisticated Java application designed to scrape, analyze, and provide insights into the Moroccan job market. It combines web scraping, data cleaning, machine learning, and a user-friendly GUI to help users explore job opportunities and understand market trends.

## Features

### 1. Multi-Source Job Scraping
- Scrapes job listings from major Moroccan job portals:
  - Rekrute
  - Anapec
  - EmploiMa
  - KhdmaMa
  - MarocAnnonces
  - MonCallCenter
  - StagairesMa
- Two-phase scraping process:
  - Links collection from job listing pages
  - Detailed information extraction from individual job posts

### 2. Intelligent Data Cleaning
- Two advanced data cleaning pipelines:
  1. GPT-4o-mini powered cleaning:
     - Structures raw scraped data into a consistent format
     - Extracts key information like skills, salary, and requirements
     - Handles special characters and encoding issues
     - Normalizes data fields for consistency
  2. RegEx-based cleaning with dictionary support:
     - Implements site-specific cleaners for each job portal
     - Extracts and structures data using regex patterns and string manipulation
     - Utilizes dictionary files for consistent extraction of skills, regions, and other key information
     - Ensures consistency across different data sources
- Secondary cleaning phase for JSON formatting and validation

### 3. Structured Database Storage
- Organized SQL database schema for job listings
- Stores comprehensive job information including:
  - Location, sector, and region
  - Salary and experience requirements
  - Required skills (both hard and soft)
  - Company information and description
  - Contract details
  - Educational requirements
  - Job title and description
  - Application and publication dates
  - Language requirements
  - Personality traits and desired profile

### 4. User-Friendly GUI
The application features a modern graphical interface with four main sections:

1. **Database Refresh**
   - Manual trigger for scraping new job listings
   - Updates database with latest market offerings

2. **Job Browser**
   - Search and filter job listings
   - View detailed job information
   - User-friendly interface for exploring opportunities

3. **AI Chatbot**
   - Intelligent interaction for job market insights
   - Natural language queries about job trends

4. **Statistics & ML**
   - Market trend analysis
   - Visual representation of job market data
   - Machine learning insights

## Technical Implementation

### Architecture
- **Scraping Module**: Java-based scrapers using JSoup for HTML parsing
- **Data Processing**: 
  - GPT-4o-mini integration for advanced text processing
  - RegEx-based cleaning using custom algorithms
  - Dictionary-based extraction for consistent data cleaning
- **Database**: SQL database with comprehensive schema
- **GUI**: Swing-based user interface with modern design

### Key Components

1. **Links Scraper**
   - Located in `LinksScraper/` directory
   - Individual scrapers for each job portal
   - Collects job posting URLs and basic information

2. **Data Scraper**
   - Located in `DataScraper/` directory
   - Extracts detailed information from individual job posts
   - Handles different website structures and formats

3. **Data Cleaning**
   - `Cleaner.java`: GPT-4o-mini powered data structuring
   - `CleanLLM.java`: JSON formatting and validation
   - `RegExCleaner.java`: RegEx-based cleaning pipeline
   - Individual cleaners for each job portal in `Cleaners/` directory
   - Dictionary files in `resources/dictionary/` for consistent data extraction
   - Ensures data consistency and quality across different sources

4. **Database Operations**
   - `InsertJson.java`: Handles database insertions
   - Proper data type conversion and error handling
   - Efficient batch processing

## Setup and Usage

### Prerequisites
- Java Development Kit (JDK) 11 or higher
- MySQL Database
- Maven for dependency management
- OpenAI API key for GPT-4o-mini integration

### Installation
1. Clone the repository
2. Configure database connection in `DatabaseConnection.java`
3. Set up OpenAI API key in `Cleaner.java`
4. Run `mvn install` to install dependencies

### Running the Application
1. Execute `App.java` to launch the GUI
2. Use the "Refresh Database" option to perform initial data collection
3. Explore jobs, insights, and analytics through the interface

## Database Schema

[Database schema section remains unchanged]

## Recent Updates

The project has been updated to include more comprehensive job information and an improved data cleaning pipeline. Key changes include:
1. Enhanced data extraction in `Cleaner.java` to capture additional job details.
2. Improved data validation and formatting in `CleanLLM.java`.
3. `RegExCleaner.java` implemented a more robust RegEx-based cleaning pipeline.
4. Removed OpenNLP dependencies in favor of custom RegEx patterns and dictionary-based extraction.
5. Enhanced individual cleaner classes for each job portal to rely more heavily on RegEx and JSON-based dictionaries.
6. Updated database insertion process in `InsertJson.java` to accommodate the new fields.
7. Expanded database schema to store more detailed job information.

These updates allow for more nuanced job market analysis and provide users with richer information about each job opportunity. The new RegEx-based cleaning pipeline with dictionary support ensures better consistency and accuracy in data extraction across different job portals.
