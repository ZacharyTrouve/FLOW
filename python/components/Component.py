class Component:
  def __init__(self, line):
    split = line.split(',', 3)
    self.ID = split[0]
    self.input_node = split[1]
    self.output_node = split[2]

  def pressure_drop(self, Q):
    pass

  def mass_flow_rate(self, pdrop):
    pass

  def update(self, current_time, pressure_drop, mass_flow_rate, upstreet_pressure):
    pass
