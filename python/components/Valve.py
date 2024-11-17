import Component
import ast
from math import sqrt, floor

class Valve(Component.Component):
  def init(self, ID, input_node, output_node, Cv, time_of_opening, time_taken_2_open, type = "Ball"):
    self.ID = ID
    self.input_node = input_node
    self.output_node = output_node
    self.state = 0
    self.Cv_open = Cv
    self.time_of_opening = time_of_opening
    self.time_taken_2_open = time_taken_2_open

  def __init__(self, line):
    split = line.split(',', 3)
    self.ID = split[0]
    self.input_node = split[1]
    self.output_node = split[2]
    dic = ast.literal_eval(split[3])
    self.state = dic['state']
    self.Cv_open = dic['Cv_open']
    self.time_of_opening = dic['time_of_opening']
    self.time_taken_2_open = dic['time_taken_2_open']
    #state,0,1,Cv_open,?,?,time_of_opening,0,inf,time_taken_2_open,0,10000

    if self.time_of_opening <= 0 and self.time_taken_2_open <= 0:
      self.current_Cv = self.Cv
    else:
      self.current_Cv = 0

    if type == "Ball":
      self.opening_curve = [0, 0.03, 0.06, 0.17, 0.25, 0.38, 0.64, 0.86, 1]

    elif type == "Butterfly":
      self.opening_curve = [0, 0.03, 0.07, 0.12, 0.22, 0.35, 0.5, 0.71, 0.93, 1]

    elif type == "Poppet":
      self.opening_curve = [0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1]

  def pdrop(self, mass_flow_rate, fluid_density, fluid_viscocity):
    return 98.776 * ((mass_flow_rate / self.current_Cv) ** 2) * fluid_density

  def mrate(self, pressure_drop, fluid_density, fluid_viscocity):
      return self.current_Cv * sqrt(pressure_drop / (98.776 * fluid_density))

  def update(self, current_time, pressure_drop, mass_flow_rate, upstreet_pressure):
    time_fraction = (current_time - self.opening) / self.time_taken_2_open

    if time_fraction >= 0:
      index = time_fraction / len(self.opening_curve)
      cv_high = self.opening_curve[floor(index) + 1] * self.Cv_open
      cv_low = self.opening_curve[floor(index)] * self.Cv_open
      self.current_Cv = (cv_high - cv_low) * (index - floor(index)) + cv_low

    else:
      self.current_Cv = 0