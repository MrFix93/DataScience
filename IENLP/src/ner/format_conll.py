
file_object = open("data/CoNLLFormat.txt", "r+")
format_file = open("data/CoNLLFormat.tsv", "w")

for line in file_object:
    values = line.split("\t")
    if values[0] == "-DOCSTART-" or len(values) != 2:
        continue
    format_file.write(line)
