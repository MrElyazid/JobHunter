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
- Advanced data cleaning pipeline using GPT-4:
  - Structures raw scraped data into a consistent format
  - Extracts key information like skills, salary, and requirements
  - Handles special characters and encoding issues
  - Normalizes data fields for consistency
- Secondary cleaning phase for JSON formatting and validation

### 3. Structured Database Storage
- Organized SQL database schema for job listings
- Stores comprehensive job information including:
  - Location and sector
  - Salary and experience requirements
  - Required skills (both hard and soft)
  - Company information
  - Contract details
  - Educational requirements

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
- **Data Processing**: Two-phase cleaning system with GPT-4 integration
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
   - `Cleaner.java`: GPT-4 powered data structuring
   - `CleanLLM.java`: JSON formatting and validation
   - Ensures data consistency and quality

4. **Database Operations**
   - `InsertJson.java`: Handles database insertions
   - Proper data type conversion and error handling
   - Efficient batch processing

## Setup and Usage

### Prerequisites
- Java Development Kit (JDK)
- MySQL Database
- Maven for dependency management
- OpenAI API key for GPT-4 integration

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

The application uses a structured database with the following key fields:
- `location`: Job location
- `sector`: Industry sector
- `job_description`: Detailed role description
- `min_salary`: Minimum salary offered
- `is_remote`: Remote work possibility
- `hard_skills`: Technical skills required
- `soft_skills`: Interpersonal skills needed
- `company`: Employer information
- `foreign_company`: International company indicator
- `contract_type`: Employment contract type
- `min_experience`: Required years of experience
- `diploma`: Educational requirements

## Contributing

Contributions are welcome! Please feel free to submit pull requests, report bugs, or suggest features.

## License

© 2023 JobHunter - All Rights Reserved
