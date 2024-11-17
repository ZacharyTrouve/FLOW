import Component
import ast
from math import sqrt

class PressureRegulator(Component.Component):
  def __init__(self, ID, input_node, output_node, max_Cv, min_Cv, inertia, damping, target_pressure):
    self.ID = ID
    self.input_node = input_node
    self.output_node = output_node
    self.max_Cv = max_Cv
    self.min_Cv = min_Cv
    self.inertia = inertia
    self.damping = damping
    self.target = target_pressure
    self.state = 0.5 #Goes from 0 to 1
    self.speed = 0 #Goes from -1 to 1
    self.time = 0
    self.opening_curve = [0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1]
    self.current_Cv = 0.5

  def __init__(self, line):
    split = line.split(',', 3)
    self.ID = split[0]
    self.input_node = split[1]
    self.output_node = split[2]
    dic = ast.literal_eval(split[3])
    self.max_Cv = dic['max_Cv']
    self.min_Cv = dic['min_Cv']
    self.inertia = dic['inertia']
    self.damping = dic['damping']
    self.target = dic['target']
    self.state = dic['state']
    self.speed = dic['speed']
    self.time = dic['time']
    self.opening_curve = dic['opening_curve']
    self.current_Cv = dic['current_Cv']
    #max_Cv,?,?,minCv,?,?,inertia,?,?,damping,?,?,target,?,?,state,0,1,speed,-1,1,time,0,inf,opening_curve,0,1,current_Cv,0,10


  def pdrop(self, mass_flow_rate, fluid_density, fluid_viscocity):
    return 0.13369 * ((mass_flow_rate / self.current_Cv ) ** 2)/ fluid_density

  def mrate(self, pressure_drop, fluid_density, fluid_viscocity):
    return 2.73495 * sqrt(fluid_density * pressure_drop) * self.current_Cv

  def update(self, current_time, pressure_drop, mass_flow_rate, upstreet_pressure):
    state = self.state + self.speed * (self.time - current_time)
    speed = self.speed + (self.target - self.pressure)
    self.time = current_time