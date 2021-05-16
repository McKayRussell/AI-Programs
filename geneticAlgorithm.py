import random

M = 20
N = 10
# initialize 2D array with random 0's and 1's
pop = [[random.randint(0, 1)for i in range(M)] for j in range(N)]
newpop = []

def main():
    newpop.clear()

    # if a row of 20 1's is found stop the program
    fits = fitness(pop)
    if(fits == -1):
        return -1
    
    # include elitism in the population by carrying the most fit individual from the...
    # ... parent array into the child array
    count = 0
    highIndex = 0
    highest = fits[0]
    for i in fits:
        if( i > highest):
            highest = i
            highIndex = count
        count += 1
        
    newpop.append(pop[highIndex])

    count = 0
    for i in pop:
        # on a random, 50/50 chance, do selection, crossover, mutation, and insert into child array
        if(random.random() > 0.5):

            parent1 = i
            parent2 = pop[selection(fits, count)]

            (child1, child2) = crossover(parent1, parent2)

            child1 = mutation(child1)
            child2 = mutation(child2)

            newpop.append(child1)
            if(len(newpop) >= len(pop)):
                break
            newpop.append(child2)
            count += 1
    
    # copy child array into parent array for next iteration
    for i in range(len(newpop)):
        for j in range(20):
            pop[i][j] = newpop[i][j]

    # display resulting array
    print(newpop)

def fitness(pop):
    fits = []
    # calculate the fitness of each row based off of how many 1's are present
    count = 0
    for i in pop:
        fit = 0
        count += 1
        for j in i:
            if(j == 1):
                fit += 1
        # if a row of 20 1's is found stop the program and print the current array
        if(fit >= 20):
            print("Array of 20 1's found in row " + str(count))
            print(pop)
            return -1
        fits.append(fit)

    return fits

def crossover(parent1, parent2):
    # take the selected parents and swap halves to create two new child rows
    childOne = parent1[0:10]
    childOne[10:20] = parent2[10:20]

    childTwo = parent2[0:10]
    childTwo[10:20] = parent1[10:20]
    return childOne, childTwo

def mutation(child):
    # on a random 10% chance, mutate the child row by flipping one random 0 or 1
    rand = random.randint(0, 19)
    if(random.random() < 0.1):
        if(child[rand] == 0):
            child[rand] = 1
        else:
            child[rand] = 0

    return child

def selection(fits, parent1):
    # select a random three rows from the 2D array as potential parents
    randIndex = []
    for i in range(15):
        rand = random.randint(0, 9)
        if(rand not in randIndex and rand != parent1):
            randIndex.append(rand)
    randIndex = randIndex[0:3]

    # order the potential parents from lowest to highset fitness values
    for i in range(3):
        for j in range(i + 1, 3):
            if(fits[randIndex[i]] > fits[randIndex[j]]):
                temp = randIndex[i]
                randIndex[i] = randIndex[j]
                randIndex[j] = temp

    # return index of most fit individual in the selected 3 potential parents
    return randIndex[2]

count = 0
for i in range(100):
    # run the algorithm 100 times stopping after 100 iterations or when a row of 20 1's is found
    count += 1
    print("Iteration " + str(count) + ":")
    stop = main()
    if(stop == -1):
        break