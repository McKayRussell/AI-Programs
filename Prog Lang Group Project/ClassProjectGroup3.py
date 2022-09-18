# course: cmps3500
# CLASS Project
# PYTHON IMPLEMENTATION: BASIC DATA ANALYSIS
# date: 04/03/22
# Student 1: Carolina Martinez
# Student 2: Jeffrey Hicks
# Student 3: McKay Russell
# description: Implements Basic Data Analysis Routines

import pandas as pd
import time

# Initialize an empty, global data frame
data = pd.DataFrame()

# Initialize time and flag variables
total_time = 0
load_time = 0
clean_time = 0
answer_time = 0
search_state_time = 0
search_year_time = 0
search_temp_time = 0
new_data_flag = 0
data_clean_flag = 0

# Function to start a timer so that times can be printed in other functions
def startTimer():
    print("************************************")
    print("-- STARTING TIMER --")
    # Starting Timer and returning resulting value to calculate total time
    start_time = time.time()
    print(f"-- RUNTIME: [{time.time() - start_time}] --\n")
    return start_time

# Function to stop the timer and calculate total time passed
def stopTimer(start_time, caller):
    global total_time
    global load_time
    global clean_time
    global answer_time
    global search_state_time
    global search_year_time
    global search_temp_time

    # Stopping Timer, calculating time for specific function and total time
    print("\n-- STOPPING TIMER --")
    print(f"-- RUNTIME: [{time.time() - start_time}] --")
    print("************************************")
    print("")

    if(caller == 1):
        load_time += time.time() - start_time
    elif(caller == 2):
        clean_time += time.time() - start_time
    elif(caller == 3):
        answer_time += time.time() - start_time
    elif(caller == 4):
        search_state_time += time.time() - start_time
    elif(caller == 5):
        search_year_time += time.time() - start_time
    elif(caller == 6):
        search_temp_time += time.time() - start_time

    total_time += time.time() - start_time

# Function to load data from files
def loadData():
    global data
    global new_data_flag
    global data_clean_flag

    print("Enter a csv file to read (entering nothing will default to loading",
            "US_Accidents_data.csv)")
    fileName = input("File Name: ")

    # Starting Timer
    start_time = startTimer()

    # If the file name is empty default to US_Accidents
    print("Loading input data set:")
    if(fileName == ""):
        # Read data in from file and store in pandas dataframe
        data = pd.read_csv('US_Accidents_data.csv', index_col = 0)
        print(f"[{time.time() - start_time}] Loading US_Accidents_data.csv")
    else:
        # Try reading file
        try:
            # Read data in from file and store in pandas dataframe
            data = pd.read_csv(fileName)
            print(f"[{time.time() - start_time}] Loading {fileName}")
        except:
            # If file can't be read make an empty data frame to avoid errors
            data = pd.DataFrame()
            # Notify user that file couldn't be read
            print("-- File entered could not be read. --")
    
    # Check if data set has columns needed to process it
    if('ID' not in data.columns or 'Severity' not in data.columns or
        'Start_Time' not in data.columns or 'End_Time' not in data.columns or
        'City' not in data.columns or 'State' not in data.columns or
        'Zipcode' not in data.columns or 'Country' not in data.columns or 
        'Temperature(F)' not in data.columns or 'Humidity(%)' not in
        data.columns or 'Visibility(mi)' not in data.columns or
        'Weather_Condition' not in data.columns):

        # If any of the columns are not found tell the user the data can't be..
        # .. used and create an empty data frame
        print("-- File not suitable for processing, necessary data not found",
            "in file. Choose another file. --")
        data = pd.DataFrame()

    # Stopping Timer
    stopTimer(start_time, 1)

    # Clear flags about if data is clean or new
    new_data_flag = 0
    data_clean_flag = 0


def dataCleanup():
    global data
    global new_data_flag
    global data_clean_flag

    # If the data is empty, don't attempt to clean it
    if(data.empty):
        print("-- ERROR: No data to clean. Load data first. --")
        return data
    # If data has already been cleaned, don't attempt to clean it
    elif(new_data_flag == 1):
        print("No new data has been loaded. Load new data before cleaning.")
        return data
    
    # Starting Timer
    start_time = startTimer()
    print("Cleaning input data set:")
    print(f"[{time.time() - start_time}] Performing Data Clean Up")

    # Drop rows that have missing values in certain columns
    data.dropna(subset=['ID', 'Severity', 'Start_Time', 'End_Time',
        'Zipcode', 'Country', 'Visibility(mi)', 'Weather_Condition'],
        inplace=True)

    # Drop rows that have more than 2 columns blank
    data.dropna(thresh=19, inplace=True)

    # Drop rows with a distance of 0
    data = data[data['Distance(mi)'] != 0]
    
    # Change zipcode to only 5 digits
    data['Zipcode'] = data['Zipcode'].str[:5]
    
    # Drop accidents that took 0 time
    data['Start_Time'] = pd.to_datetime(data['Start_Time'])
    data['End_Time'] = pd.to_datetime(data['End_Time'])
    data = data[(data['End_Time']-data['Start_Time']) != "0 days 00:00:00"]
    
    # Print timing of data clean and resulting amount of rows
    print(f"[{time.time() - start_time}] Printing row count after data",
        "clean is finished")
    print(f"[{time.time() - start_time}] {len(data.index)} rows")

    # Stopping Timer
    stopTimer(start_time, 2)

    # Set flags signifying data has been cleaned and is not new
    new_data_flag = 1
    data_clean_flag = 1


def answerQuestions():
    global data

    # If data is empty, don't attempt to answer questions
    if(data.empty):
        print("-- ERROR: No data. Load data first. --")
        return data
    # If data has not been cleaned, do necessary reformating
    elif(data_clean_flag != 1):
        print("-- Data has NOT been cleaned. Data SHOULD be cleaned before",
            "answering questions. --")
        data['Start_Time'] = pd.to_datetime(data['Start_Time'])
        data['End_Time'] = pd.to_datetime(data['End_Time'])

    # Starting Timer
    start_time = startTimer()

    print("Answering questions:")
    print("********************")
    
    ###########################################################
    #                          Question 1

    print(f"[{time.time() - start_time}]",
    "1. In what month were there more accidents reported?")

    months = ['January', 'February', 'March', 'April', 'May', 'June', 'July',
        'August', 'September', 'October', 'November', 'December']

    # Create table of months and the amount of times they occurred
    highest_month = data['Start_Time'].dt.month.value_counts()

    # Isolate index of highest reocurring month
    try:
        month_index = highest_month.index.tolist()[0]
    except: # For the very unlikely case that the try branch fails
        months = ['No months found']
        highest_month = ['more than 0', 'more than 0']
        month_index = 1
    
    print(f"[{time.time() - start_time}] {months[month_index-1]}",
    f"with {highest_month[month_index]} total reports.\n")

    ###########################################################
    #                          Question 2

    print(f"[{time.time() - start_time}]",
    "2. What is the state that had the most accidents in 2020?")

    # Copy columns into new data frame
    state_2020 = data.loc[:, ('Start_Time', 'State')]
    # Remove rows where the year is not 2020
    state_2020 = state_2020 [state_2020['Start_Time'].dt.year == 2020]
    # Set variable that returns highest state value and the amount of reports
    try:
        top_state = state_2020['State'].value_counts().index.tolist()[0]
        state_counts = state_2020['State'].value_counts()[0]
    except:
        top_state = "No state found"
        state_counts = "0 or more"

    print(f"[{time.time() - start_time}] {top_state} with {state_counts}",
    "total reports in 2020.\n")

    ###########################################################
    #                          Question 3

    print(f"[{time.time() - start_time}]",
    "3. What is the state that had the most accidents of severity 2 in 2021?")

    # Copy columns into new data frame
    state_2021 = data.loc[:, ('Severity', 'Start_Time', 'State')]
    # Remove rows where the year is not 2021
    state_2021 = state_2021 [state_2021['Start_Time'].dt.year == 2021]
    # Remove rows where the severity is not 2
    state_2021 = state_2021 [state_2021['Severity'] == 2]
    # Set variable that returns highest state value and the amount of reports
    try:
        top_state = state_2021['State'].value_counts().index.tolist()[0]
        state_counts = state_2021['State'].value_counts()[0]
    except:
        top_state = "No state found"
        state_counts = "0 or more"

    print(f"[{time.time() - start_time}] {top_state} with {state_counts}",
    "total reports of severity 2 in 2020.\n")

    ###########################################################
    #                          Question 4

    print(f"[{time.time() - start_time}]",
    "4. What severity is the most common in Virginia?")

    # Copy necessary columns and only store rows that have VA as state
    sev_Virginia = data.loc[:, ('Severity', 'State')]
    sev_Virginia = sev_Virginia [sev_Virginia['State'] == "VA"]
    
    try:
        top_sev = sev_Virginia['Severity'].value_counts().index.tolist()[0]
        sev_counts = sev_Virginia['Severity'].value_counts()[top_sev]
    except:
        top_sev = "not found"
        sev_counts = "0 or more"

    print(f"[{time.time() - start_time}] Severity {top_sev} with",
    f"{sev_counts} occurrences in Virginia.\n")

    ###########################################################
    #                          Question 5

    print(f"[{time.time() - start_time}]",
    "5. What are the 5 cities that had the most accidents in 2019 in",
    "California?")

    # Copy necessary files and get only rows with 2019 and CA in them
    cali_2019 = data.loc[:, ('Start_Time', 'City', 'State')]
    cali_2019 = cali_2019 [cali_2019['Start_Time'].dt.year == 2019]
    cali_2019 = cali_2019 [cali_2019['State'] == "CA"]
    try:
        top_cities = cali_2019['City'].value_counts().index.tolist()[:5]
        city_counts = cali_2019['City'].value_counts()[:5]
        #Print results
        print(f"[{time.time() - start_time}] {top_cities[0]} with",
        f"{city_counts[top_cities[0]]} reports, {top_cities[1]} with",
        f"{city_counts[top_cities[1]]} reports, {top_cities[2]} with",
        f"{city_counts[top_cities[2]]} reports, {top_cities[3]} with",
        f"{city_counts[top_cities[3]]} reports, and {top_cities[4]} with",
        f"{city_counts[top_cities[4]]} reports.\n")
    except:
        print(f"[{time.time() - start_time}]",
        "-- Not enough data to answer the question. --\n")

    ###########################################################
    #                          Question 6

    print(f"[{time.time() - start_time}]",
    "6. What was the average humidity and average temperature of all",
    "accidents of severity 4 that occurred in 2021?")

    # Copy necessary columns and gather only rows with sev for and year 2021
    temp_2021 = data.loc[:, ('Severity', 'Start_Time', 'Temperature(F)', 
        'Humidity(%)')]
    temp_2021 = temp_2021 [temp_2021['Severity'] == 4]
    temp_2021 = temp_2021 [temp_2021['Start_Time'].dt.year == 2021]

    # Calculate mean value
    av_humidity = round(temp_2021['Humidity(%)'].mean(), 2)
    av_temp = round(temp_2021['Temperature(F)'].mean(), 2)

    # If no value is found, store a replacement value
    if(pd.isna(av_humidity)):
        av_humidity = "unable to be calculated"
    if(pd.isna(av_temp)):
        av_temp = "unable to be calculated"
    
    print(f"[{time.time() - start_time}] Average humidity was",
    f"{av_humidity} and average temperature was {av_temp} for all",
    "accidents of severity 4 that occurred in 2021.\n")

    ###########################################################
    #                          Question 7

    print(f"[{time.time() - start_time}]",
    "7. What are the 3 most common weather conditions (weather_conditions)",
    "when accidents occurred?")

    # Copy necessary column and get top 3 values
    top_weather = data.loc[:, ('Weather_Condition')]
    top_weather = top_weather.value_counts()[:3]

    # Print the values unless no values were stored
    try:
        print(f"[{time.time() - start_time}] The 3 most common weather",
        f"conditions were {top_weather.index.tolist()[0]} with",
        f"{top_weather[0]} occurrences, {top_weather.index.tolist()[1]}",
        f"with {top_weather[1]} occurrences, and",
        f"{top_weather.index.tolist()[2]} with {top_weather[2]} occurrences."
        "\n")
    except:
        print(f"[{time.time() - start_time}]",
            "-- Not enough data to answer the question. --\n")

    ###########################################################
    #                          Question 8

    print(f"[{time.time() - start_time}]",
    "8. What was the maximum visibility of all accidents of severity 2 that",
    "occurred in the state of New Hampshire?")

    # Copy necessary columns and apply constraints to get correct rows
    NH_vis = data.loc[:, ('Severity', 'State', 'Visibility(mi)')]
    NH_vis = NH_vis [NH_vis['Severity'] == 2]
    NH_vis = NH_vis [NH_vis['State'] == "NH"]
    max_vis = NH_vis['Visibility(mi)'].max()
    NH_vis = NH_vis['Visibility(mi)'].value_counts()

    try:
        print(f"[{time.time() - start_time}] The max visibility of all",
        f"accidents of severity 2 in New Hampshire is {max_vis} with",
        f"{NH_vis[max_vis]} occurrences.\n")
    except:
        print(f"[{time.time() - start_time}]",
            "-- Not enough data to answer the question. --\n")

    ###########################################################
    #                          Question 9

    print(f"[{time.time() - start_time}]",
    "9. How many accidents of each severity were recorded in Bakersfield?")

    # Copy necessary columns and apply constraints to get correct rows
    sev_Bako = data.loc[:, ('Severity', 'City')]
    sev_Bako = sev_Bako [sev_Bako['City'] == "Bakersfield"]
    sev_Bako = sev_Bako['Severity'].value_counts()

    # Print values unless they are out of range
    try:
        print(f"[{time.time() - start_time}] In Bakersfield there were",
        f"{sev_Bako[sev_Bako.index.tolist()[0]]} accidents of severity",
        f"{sev_Bako.index.tolist()[0]},",
        f"{sev_Bako[sev_Bako.index.tolist()[1]]} of severity",
        f"{sev_Bako.index.tolist()[1]}, and",
        f"{sev_Bako[sev_Bako.index.tolist()[2]]} of severity", 
        f"{sev_Bako.index.tolist()[2]}.\n")
    except:
        print(f"[{time.time() - start_time}]",
            "-- Not enough data to answer the question. --\n")

    ###########################################################
    #                          Question 10

    print(f"[{time.time() - start_time}]",
    "10. What was the longest accident (in hours) recorded in Florida in the",
    "Spring (March, April, and May) of 2020?")

    # Copy necessary columns and apply constraints to get correct rows
    longest_Flo = data.loc[:, ('Start_Time', 'End_Time', 'State')]
    longest_Flo = longest_Flo [longest_Flo['State'] == "FL"]
    longest_Flo = longest_Flo [longest_Flo['Start_Time'].dt.year == 2020]
    longest_Flo = longest_Flo [longest_Flo['Start_Time'].dt.month < 6]
    longest_Flo = longest_Flo [longest_Flo['Start_Time'].dt.month > 2]
    longest_Flo = str((longest_Flo['End_Time'] - 
                       longest_Flo['Start_Time']).max())

    # If no value is stored don't attempt to calculate hours
    if longest_Flo == "NaT":
        hours_Flo = 0
        print(f"[{time.time() - start_time}]",
            "-- Insufficient data to calculate. Total hours will be 0 --")
    else:
        days_to_hours = int(longest_Flo.split()[0]) * 24
        longest_Flo = longest_Flo.split()[2]
        hours_Flo = int(longest_Flo.split(":")[0])
        minutes_to_hours = int(longest_Flo.split(":")[1]) / 60
        seconds_to_hours = int(longest_Flo.split(":")[2]) / 3600
        hours_Flo = (days_to_hours + hours_Flo +
                    minutes_to_hours + seconds_to_hours)

    print(f"[{time.time() - start_time}] The longest accident in Spring of",
    f"2022 in Florida was approximately {round(hours_Flo, 4)} hours long.")
    
    # Stopping Timer
    stopTimer(start_time, 3)


def stateSearch():
    global data

    # If data is empty don't attempt to search
    if(data.empty):
        print("-- ERROR: No data to search. Load data first. --")
        return data
    # If data has not been cleaned warn and reformat
    elif(data_clean_flag != 1):
        print("-- Data has NOT been cleaned. Data SHOULD be cleaned before",
            "searching accidents. --")
        data['Start_Time'] = pd.to_datetime(data['Start_Time'])
        data['End_Time'] = pd.to_datetime(data['End_Time'])

    # Get user input for the search
    print("Search Accidents:")
    print("*****************")
    inState = input("  Enter a State name:     ")
    inCity = input("  Enter a City name:      ")
    inZIP = input("  Enter a ZIP Code:       ")

    #################################################

    # Starting Timer here because we only want to time the query of the data
    start_time = startTimer()

    # Copy required columns of data frame
    ac_search = data.loc[:, ('State', 'City', 'Zipcode')]

    # If input is empty set output to be empty, otherwise fill output string
    if(inState == ""):
        outState = ""
    else:
        outState = f"in state: {inState} "
        ac_search = ac_search [ac_search['State'] == inState]
    if(inCity == ""):
        outCity = ""
    else:
        ac_search = ac_search [ac_search['City'] == inCity]
        outCity = f"in city: {inCity} "
    if(inZIP == ""):
        outZIP = ""
    else:
        outZIP = f"in ZIP: {inZIP} "
        ac_search = ac_search [ac_search['Zipcode'] == inZIP]
    
    # Count amount of accidents
    accident_count = len(ac_search.index)

    # Print results, if all inputs are empty print total amount
    if(inState == "" and inCity == "" and inZIP == ""):
        accident_count = len(data.index)
        output = f"There were {accident_count} accidents overall."
    else:
        output = f"There were {accident_count} accidents in "
        output = output + outState + outCity + outZIP

    print(f"[{time.time() - start_time}] {output}")
    
    # Stopping Timer
    stopTimer(start_time, 4)

def yearSearch():
    global data

    # If data is empty don't attempt to search
    if(data.empty):
        print("-- ERROR: No data to search. Load data first. --")
        return data
    # If data has not been cleaned warn and reformat
    elif(data_clean_flag != 1):
        print("-- Data has NOT been cleaned. Data SHOULD be cleaned before",
            "searching accidents. --")
        data['Start_Time'] = pd.to_datetime(data['Start_Time'])
        data['End_Time'] = pd.to_datetime(data['End_Time'])

    # Get user input for the search
    print("Search Accidents:")
    print("*****************")
    inYear = input("  Enter a year (2016-2021):   ")
    inMonth = input("  Enter a month:              ")
    inDay = input("  Enter a day:                ")

    # Set flags for if input is empty
    emptyYearVal = 0
    emptyMonthVal = 0
    emptyDayVal = 0
    try:
        inYear = int(inYear)
    except ValueError:
        emptyYearVal = 1
    try:
        inMonth = int(inMonth)
    except ValueError:
        emptyMonthVal = 1
    try:
        inDay = int(inDay)
    except ValueError:
        emptyDayVal = 1

    #################################################

    # Starting Timer here because we only want to time the query of the data
    start_time = startTimer()

    # Copy and format required data
    ac_search = pd.DataFrame(columns=["Year", "Month", "Day"])
    ac_search['Year'] = data['Start_Time'].dt.year
    ac_search['Month'] = data['Start_Time'].dt.month
    ac_search['Day'] = data['Start_Time'].dt.day

    # Fill variables for output
    if(emptyYearVal == 0):
        ac_search = ac_search [ac_search['Year'] == inYear]
        outYear = f"the year: {inYear} "
    else:
        outYear = ""
    if(emptyMonthVal == 0):
        ac_search = ac_search [ac_search['Month'] == inMonth]
        outMonth = f"the month: {inMonth} "
    else:
        outMonth = ""
    if(emptyDayVal == 0):
        ac_search = ac_search [ac_search['Day'] == inDay]
        outDay = f"the day: {inDay} "
    else:
        outDay = ""
    
    # Count amount of accidents
    accident_count = len(ac_search.index)

    if(emptyYearVal != 0 and emptyMonthVal != 0 and emptyDayVal != 0):
        output = f"There were {accident_count} accidents overall."
    else:
        output = f"There were {accident_count} accidents in "
        output = output + outYear + outMonth +  outDay

    print(f"[{time.time() - start_time}] {output}")

    stopTimer(start_time, 5)

def tempSearch():
    global data

    # If data is empty don't attempt to search
    if(data.empty):
        print("-- ERROR: No data to search. Load data first. --")
        return data
    # If data has not been cleaned warn and reformat
    elif(data_clean_flag != 1):
        print("-- Data has NOT been cleaned. Data SHOULD be cleaned before",
            "searching accidents. --")
        data['Start_Time'] = pd.to_datetime(data['Start_Time'])
        data['End_Time'] = pd.to_datetime(data['End_Time'])

    print("Search Accidents:")
    print("*****************")
    print("Enter a temperature range:")
    inTempLow = input("  Lower bound:   ")
    inTempHigh = input("  Upper bound:   ")
    print("Enter a visibility range:")
    inVisLow = input("  Lower bound:   ")
    inVisHigh = input("  Upper bound:   ")

    # Copy required columns of data frame
    ac_search = data.loc[:, ('Temperature(F)', 'Visibility(mi)')]

    # Get input values or min or max of variable
    try:
        inTempLow = float(inTempLow)
    except ValueError:
        inTempLow = ac_search['Temperature(F)'].min()
    try:
        inTempHigh = float(inTempHigh)
    except ValueError:
        inTempHigh = ac_search['Temperature(F)'].max()
    try:
        inVisLow = float(inVisLow)
    except ValueError:
        inVisLow = ac_search['Visibility(mi)'].min()
    try:
        inVisHigh = float(inVisHigh)
    except ValueError:
        inVisHigh = ac_search['Visibility(mi)'].max()

    #################################################

    # Starting Timer again because we only want to time the query of the data
    start_time = startTimer()

    # Copy and format required data
    ac_search = ac_search [ac_search['Temperature(F)'] >= inTempLow]
    ac_search = ac_search [ac_search['Temperature(F)'] <= inTempHigh]
    ac_search = ac_search [ac_search['Visibility(mi)'] >= inVisLow]
    ac_search = ac_search [ac_search['Visibility(mi)'] <= inVisHigh]

    # Number of all accidents
    accident_count = len(ac_search.index)

    print(f"[{time.time() - start_time}] {accident_count}",
    f"accident(s) with temps in the range of {inTempLow} to {inTempHigh}",
    f"degrees and visibility in the range of {inVisLow} to {inVisHigh}")
    
    # Stopping Timer
    stopTimer(start_time, 6)

def menu():
    menuOption = '-1'
    loadCount = 0
    load_av = 0
    cleanCount = 0
    clean_av = 0
    answerCount = 0
    answer_av = 0
    searchStateCount = 0
    search_state_av = 0
    searchYearCount = 0
    search_year_av = 0
    searchTempCount = 0
    search_temp_av = 0
    
    # Print menu in while loop until 7 is entered
    while(menuOption != '7'):
        print("\n----           MENU:           ----")
        print("(1) Load data")
        print("(2) Process data")
        print("(3) Print Answers")
        print("(4) Search Accidents (Use City, State, and Zip Code)")
        print("(5) Search Accidents (Year, Month and Day)")
        print("(6) Search Accidents (Temperature Range and Visibility Range)")
        print("(7) Quit")
        print("------                       ------")
        menuOption = input("Enter an option:  ")
        print("\n")

        # Call corresponding function and increase amount of times that..
        # .. function has been called for later average times
        if(menuOption == '1'):
            loadData()
            loadCount += 1
        elif(menuOption == '2'):
            dataCleanup()
            cleanCount += 1
        elif(menuOption == '3'):
            answerQuestions()
            answerCount += 1
        elif(menuOption == '4'):
            stateSearch()
            searchStateCount += 1
        elif(menuOption == '5'):
            yearSearch()
            searchYearCount += 1
        elif(menuOption == '6'):
            tempSearch()
            searchTempCount += 1
    
    # Print timing results of each function and total time overall
    print("\n----           TIMING RESULTS:           ----")
    print("Total runtime of program parts:", total_time, "\n")
    if(loadCount != 0):
        load_av = load_time/loadCount
        print(f"Load data chosen {loadCount} time(s), with average runtime",
            f"of {load_av}")
    else:
        print("Load data chosen 0 times.")
    if(cleanCount != 0):
        clean_av = clean_time/cleanCount
        print(f"Clean data chosen {cleanCount} time(s), with average runtime",
            f"of {clean_av}")
    else:
        print("Clean data chosen 0 times.")
    if(answerCount != 0):
        answer_av = answer_time/answerCount
        print(f"Answer questions chosen {answerCount} time(s), with average",
            f"runtime of {answer_av}")
    else:
        print("Answer questions chosen 0 times.")
    if(searchStateCount != 0):
        search_state_av = search_state_time/searchStateCount
        print(f"State search chosen {searchStateCount} time(s), with average",
            f"runtime of {search_state_av}")
    else:
        print("State search chosen 0 times.")
    if(searchYearCount != 0):
        search_year_av = search_year_time/searchYearCount
        print(f"Year search chosen {searchYearCount} time(s), with average",
            f"runtime of {search_year_av}")
    else:
        print("Year search chosen 0 times.")
    if(searchTempCount != 0):
        search_temp_av = search_temp_time/searchTempCount
        print(f"Temp search chosen {searchTempCount} time(s), with average",
            f"runtime of {search_temp_av}")
    else:
        print("Temp search chosen 0 times.")

    total_av = (load_av + clean_av + answer_av + search_state_av +
                search_year_av + search_temp_av)
    print(f"Total average time: {total_av}")
    print("------                                 ------\n")

menu()