import argparse

parser = argparse.ArgumentParser(description='Check the result of Algorithm A against a reference')
parser.add_argument('reference')
parser.add_argument('result')

args = parser.parse_args()

expected = {}
curr_pp = ''

with open(args.reference) as f:
	for line in f:
		if line.strip()[:2] == 'BB':
			curr_pp = line.strip()
			expected[curr_pp] = {}
		elif ' -> ' in line: 
			lhs, rhs = line.split(' -> ')
			expected[curr_pp][lhs] = set(rhs.strip()[1:-1].split(', '))

found = {}
curr_pp = ''

with open(args.result) as f:
	for line in f: 
		if line.strip()[:2] == 'BB':
			curr_pp = line.strip()
			found[curr_pp] = {}
		elif ' -> ' in line: 
			lhs, rhs = line.split(' -> ')
			found[curr_pp][lhs] = set(rhs.strip()[1:-1].split(', '))

if set(expected.keys()) != set(found.keys()):
	print 'Program Points do not match:'
	print 'Expected:', expected.keys()
	print 'Found:', found.keys()
else:
	for pp in expected:
		print pp
		if set(expected[pp].keys()) != set(found[pp].keys()):
			print 'Key sets do not match:'
			print 'Expected:', expected[pp].keys()
			print 'Found:', found[pp].keys()
		else:
			for key in expected[pp]:
				if expected[pp][key] != found[pp][key]:
					print 'Points to set does not match for variable `%s`: ' % key
					print 'Expected:', expected[pp][key]
					print 'Found:', found[pp][key]
		print 
