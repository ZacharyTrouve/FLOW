from .component import Component
import ast
from math import sqrt

class CheckValve(Component):
  '''def __init__(self, ID, input_node, output_node, Cv_open, Cv_closed = 0.00000001, initial_state = 0):
    self.ID = ID
    self.input_node = input_node
    self.output_node = output_node
    self.Cv_open = Cv_open
    self.Cv_closed = Cv_closed
    self.state = initial_state'''

  def __init__(self, line):
    split = line.split(',', 3)
    self.ID = split[0]
    self.input_node = split[1]
    self.output_node = split[2]
    dic = ast.literal_eval(split[3])
    self.Cv_open = float(dic['Cv_open'])
    self.Cv_closed = float(dic['Cv_closed'])
    self.state = float(dic['state'])

  def pdrop(self, mass_flow_rate, fluid_density, fluid_viscocity):
    if self.state == 1:
      return 0.13369 * ((mass_flow_rate / self.Cv_open ) ** 2)/ fluid_density
    else:
      return 0.13369 * ((mass_flow_rate / self.Cv_closed ) ** 2)/ fluid_density

  def mrate(self, pressure_drop, fluid_density, fluid_viscocity):
    if self.state == 1:
      return 2.73495 * sqrt(fluid_density * pressure_drop) * self.Cv_open
    else:
      return 2.73495 * sqrt(fluid_density * pressure_drop) * self.Cv_closed

  def update(self, current_time, pressure_drop, mass_flow_rate, upstreet_pressure):
    ## valve flows only one way if pressure allows, otherwise doesn't flow at all
    if pressure_drop > 0:
      self.state = 1
    else:
      self.state = 0
