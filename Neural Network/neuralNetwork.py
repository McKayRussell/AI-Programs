import csv
import math
from random import seed 
from random import random

# read the csv file (in this case iris.csv) into a 2D array
FILENAME = 'iris.csv'
with open(FILENAME, newline='') as csvfile:
	data = []
	for row in csv.reader(csvfile, delimiter=','):
		data.append(row)

# initialize random weights and thetas
seed(1)
# Hidden neurons #
w0 = [random() for i in range(4)]
theta0 = random()
w1 = [random() for i in range(4)]
theta1 = random()
w2 = [random() for i in range(4)]
theta2 = random()
#Output neurons #
w3 = [random() for i in range(3)]
theta3 = random()
w4 = [random() for i in range(3)]
theta4 = random()
w5 = [random() for i in range(3)]
theta5 = random()

alpha = 0.1

def neuron(w0, w1, w2, w3, w4, w5, theta0, theta1, theta2, theta3, theta4, theta5, alpha, data):
	# run the algorithm for epochs, each epoch consisting of 150 iterations over the data array
	for epoch in range(100):
		total = 0
		i = 0

		# for each row in the data carry out forward feeding and backpropagation
		for x in data:
			# create an array of the values from the csv that coorelate to which flower type the current row is
			yd = x[4:7]
			
			# calculate charge values for the input neurons given the input values from the csv file
			X0 = float(x[0]) * w0[0] + float(x[1]) * w0[1] + float(x[2]) * w0[2] + float(x[3]) * w0[3] - theta0
			X1 = float(x[0]) * w1[0] + float(x[1]) * w1[1] + float(x[2]) * w1[2] + float(x[3]) * w1[3] - theta1
			X2 = float(x[0]) * w2[0] + float(x[1]) * w2[1] + float(x[2]) * w2[2] + float(x[3]) * w2[3] - theta2

			# plug the charge values into the activation function to get the output
			y0 = 1/(1 + math.exp(-X0))
			y1 = 1/(1 + math.exp(-X1))
			y2 = 1/(1 + math.exp(-X2))
			
			# use the output of the input neurons to calculate charge for the output neurons
			X3 = y0 * w3[0] + y1 * w3[1] + y2 * w3[2] - theta3
			X4 = y0 * w4[0] + y1 * w4[1] + y2 * w4[2] - theta4
			X5 = y0 * w5[0] + y1 * w5[1] + y2 * w5[2] - theta5

			# again use the resulting charge values and plug them into the activation function for the...
			# ... final guesses of this iteration of the neural network
			Output3 = 1/(1 + math.exp(-X3))
			Output4 = 1/(1 + math.exp(-X4))
			Output5 = 1/(1 + math.exp(-X5))

			# backpropagate from right to left adjusting the weights used in the charge calculations...
			# ... for the first output neuron
			delta3 = Output3 * (1 - Output3) * (int(yd[0]) - Output3)
			w3[0] += alpha * y0 * delta3
			w3[1] += alpha * y1 * delta3
			w3[2] += alpha * y2 * delta3
			theta3 -= alpha * delta3

			# repeat the process for the other two output neurons
			delta4 = Output4 * (1 - Output4) * (int(yd[1]) - Output4)
			w4[0] += alpha * y0 * delta4
			w4[1] += alpha * y1 * delta4
			w4[2] += alpha * y2 * delta4
			theta4 -= alpha * delta4

			delta5 = Output5 * (1 - Output5) * (int(yd[2]) - Output5)
			w5[0] += alpha * y0 * delta5
			w5[1] += alpha * y1 * delta5
			w5[2] += alpha * y2 * delta5
			theta5 -= alpha * delta5
			
			# use the adjusted values from the output neurons to adjust the weights for the input neurons
			delta0 = y0 * (1 - y0) * ( w3[0] * delta3 + w4[0] * delta4 + w5[0] * delta5)
			w0[0] += alpha * float(x[0]) * delta0
			w0[1] += alpha * float(x[1]) * delta0
			w0[2] += alpha * float(x[2]) * delta0
			w0[3] += alpha * float(x[3]) * delta0
			theta0 -= alpha * delta0

			delta1 = y1 * (1 - y1) * ( w3[1] * delta3 + w4[1] * delta4 + w5[1] * delta5)
			w1[0] += alpha * float(x[0]) * delta1
			w1[1] += alpha * float(x[1]) * delta1
			w1[2] += alpha * float(x[2]) * delta1
			w1[3] += alpha * float(x[3]) * delta1
			theta1 -= alpha * delta1

			delta2 = y2 * (1 - y2) * ( w3[2] * delta3 + w4[2] * delta4 + w5[2] * delta5)
			w2[0] += alpha * float(x[0]) * delta2
			w2[1] += alpha * float(x[1]) * delta2
			w2[2] += alpha * float(x[2]) * delta2
			w2[3] += alpha * float(x[3]) * delta2
			theta2 -= alpha * delta2

			# keep a total of the error for the mean absolute deviation (or MAD) value calculation
			total += abs(int(yd[0]) - Output3) + abs(int(yd[1]) - Output4) + abs(int(yd[2]) - Output5)
			
			# print the predictions made by the neural network for the current iteration
			i += 1
			prediction = [round(Output3, 2), round(Output4, 2), round(Output5, 2)]
			print("Epcoch " + str(epoch) + ", Iteration  " + str(i) + ": Prediction is " + str(prediction))
		
		# finish calculating the MAD value dividing the total error by the length of the array
		accuracy = (total/len(data))
		print("Epoch " + str(epoch) + " Results: MAD =  " + str(accuracy))

neuron(w0, w1, w2, w3, w4, w5, theta0, theta1, theta2, theta3, theta4, theta5, alpha, data)