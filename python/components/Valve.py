from .component import Component
from math import sqrt, floor
import ast
import numpy as np

class Valve(Component):
  '''def init(self, ID, input_node, output_node, Cv, time_of_opening, time_taken_2_open, type, ):
    self.ID = ID
    self.input_node = input_node
    self.output_node = output_node
    self.state = 0
    self.Cv_open = Cv
    self.time_of_opening = time_of_opening
    self.time_taken_2_open = time_taken_2_open'''

  def __init__(self, line):
    split = line.split(',', 3)
    self.ID = split[0]
    self.input_node = split[1]
    self.output_node = split[2]
    dic = ast.literal_eval(split[3])
    self.state = float(dic['state'])
    self.Cv_open = float(dic['Cv_open'])
    self.time_of_opening = float(dic['time_of_opening'])
    self.time_taken_2_open = float(dic['time_taken_2_open'])
    self.type = dic['type']

    #state,0,1,Cv_open,0,99999,time_of_opening,0,999999999,time_taken_2_open,0,10000

    if self.time_of_opening <= 0 and self.time_taken_2_open <= 0:
      self.current_Cv = self.Cv # self.Cv not a thing
    else:
      self.current_Cv = 0

    # if type == None: # default to Ball valve
      # self.opening_curve = [0, 0.03, 0.06, 0.17, 0.25, 0.38, 0.64, 0.86, 1]

    if type == "Ball":
      self.opening_curve = [0, 0.03, 0.06, 0.17, 0.25, 0.38, 0.64, 0.86, 1]

    elif type == "Butterfly":
      self.opening_curve = [0, 0.03, 0.07, 0.12, 0.22, 0.35, 0.5, 0.71, 0.93, 1]

    elif type == "Poppet":
      self.opening_curve = [0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1]

    else:
      # model Ball valve as a default
      self.opening_curve = [0, 0.03, 0.06, 0.17, 0.25, 0.38, 0.64, 0.86, 1]

  def pdrop(self, mass_flow_rate, fluid_density, fluid_viscocity):
    return 98.776 * ((mass_flow_rate / self.current_Cv) ** 2) * fluid_density

  def mrate(self, pressure_drop, fluid_density, fluid_viscocity):
      return  np.sign(pressure_drop / (98.776 * fluid_density)) * self.current_Cv * sqrt(abs(pressure_drop / (98.776 * fluid_density)))

  def update(self, current_time, pressure_drop, mass_flow_rate, upstreet_pressure):
    time_fraction = (current_time - self.time_of_opening) / self.time_taken_2_open # self.opening --> self.time_of_opening, I assume the goal of the pair in parentheses is to get the timestep?

# has Vincent tested this? I'm not confident it works but I also can't tell exactly what its supposed to do
    if time_fraction >= 0:
      index = time_fraction * len(self.opening_curve)

      if index > len(self.opening_curve) - 1:
        self.current_Cv = self.Cv_open
      else:
        cv_high = self.opening_curve[floor(index) + 1] * self.Cv_open
        cv_low = self.opening_curve[floor(index)] * self.Cv_open
        self.current_Cv = (cv_high - cv_low) * (index - floor(index)) + cv_low

    else:
      self.current_Cv = 0
