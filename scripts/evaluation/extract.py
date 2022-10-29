import sys
import json
import os
import regex as re
import pandas as pd


known_transformers = [
    "RenameVariableTransformer", "IfFalseElseTransformer", "AddNeutralElementTransformer",
    "AddUnusedVariableTransformer", "LambdaIdentityTransformer",
    "IfTrueTransformer", "RandomParameterNameTransformer"
]

def get_known_transformers() -> [str]:
    return known_transformers

def make_csv(path_to_data_dir: str, filename: str = "results.csv") -> None:
    """
    Extracts all data from the given dirs jsonfiles,
    prints them to a .csv file nearby
    :param path_to_data_dir:
    :param filename:
    :return: Nothing, but write file next to it
    """
    df = make_df(path_to_data_dir)
    df.to_csv(filename)


def make_df(path_to_data_dir: str) -> pd.DataFrame:
    """
    Walks over the given datapath and finds all .json files from the Guided-MT-Code2Vec Experiment.
    The path-names are very important, as the experiment and generation are extracted from the path.
    Please consult the nearby README.md for expected Folder-Layout.
    """
    json_files: [str] = []

    # iterate over files in
    # that directory
    for root, dirs, files in os.walk(path_to_data_dir):
        for filename in files:
            if ".json" in filename:
                json_files.append(os.path.join(root, filename))

    print(f"found {len(json_files)} .json-files in {path_to_data_dir}")

    datapoints = []
    for file in json_files:
        with open(file) as f:
            datapoint = json.loads(f.read())
            datapoint["path"] = file
            datapoint["seed"] = extract_seed_from_path(file)
            datapoint["experiment"] = extract_experiment_from_path(path_to_data_dir, file)
            datapoint["TRANSFORMATIONS"] = count_transformers(datapoint)
            datapoint["generation"] = extract_generation_from_path(file)
<<<<<<< HEAD
            datapoint["algorithm"] = extract_algorithm_from_experiment_name(datapoint["experiment"])
=======
>>>>>>> 5bd83ca2d05ca668b18a7b67d2a0ffd340fa9f91
            transformers = extract_transformers_from_genotype(datapoint["genotype"], known_transformers)
            datapoint = {**datapoint, **transformers}
            del transformers

            datapoints.append(datapoint)

    df = pd.DataFrame(datapoints)
    # Change some Datatypes
    df["algorithm"] = df.algorithm.astype("category")
    df['experiment'] = df.experiment.astype('category')
    df['seed'] = df.seed.astype('category')

    return df


def extract_seed_from_path(path: str) -> int:
    pattern = r'seed-\d+'
    match = re.findall(pattern, path)[0]
    return int(match[5:])


def extract_experiment_from_path(directory: str, path: str) -> str:
    pattern = directory + r'.*?/seed'
    match = re.findall(pattern, path)[0]
    return match[len(directory)+1:-5]


def extract_generation_from_path(path: str) -> int:
    pattern = r'gen\d+'
    match = re.findall(pattern, path)[0]
    return int(match[3:])


def count_transformers(datapoint):
    # There was an issue with the json, the genotype is just a string as some quotes were missing
    raw = datapoint["genotype"]
    if type(raw) == str:
        pattern = "transformer"
        matches = re.findall(pattern, raw)
        return len(matches)
    else:
        return len(raw)

<<<<<<< HEAD
def extract_algorithm_from_experiment_name(exp_name:str)->str:
    if "random" in exp_name:
        return "random"
    else:
        return "genetic"
=======
>>>>>>> 5bd83ca2d05ca668b18a7b67d2a0ffd340fa9f91

def extract_transformers_from_genotype(genotype, transformers: [str] = known_transformers) -> dict:
    if type(genotype) == str:
        return extract_transformers_from_str(genotype,transformers)
    else:
        return extract_genotype_from_json(genotype,transformers)


def extract_transformers_from_str(genotype:str, transformers: [str] = known_transformers) -> dict:
    """
    Tries to count the given Transformers in the given Genotype-Str.
    Result is a dictionary with the count of each Transformer.

    >>> example_genotype = "[{ transformer: RenameVariableTransformer, seed: 1131100509 }{ transformer: AddNeutralElementTransformer, seed: -1887344816 }{ transformer: IfFalseElseTransformer, seed: -1554943859 }{ transformer: LambdaIdentityTransformer, seed: 2097957312 }{ transformer: AddUnusedVariableTransformer, seed: 2076014978 }]"
    >>> transformers = [ "RenameVariableTransformer","IfFalseElseTransformer","AddNeutralElementTransformer","AddUnusedVariableTransformer","LambdaIdentityTransformer","IfTrueTransformer", "RandomParameterNameTransformer"]
    >>> extract_transformers_from_str(example_genotype,transformers))
    {'RenameVariableTransformer': 1,
     'IfFalseElseTransformer': 1,
     'AddNeutralElementTransformer': 1,
     'AddUnusedVariableTransformer': 1,
     'LambdaIdentityTransformer': 1,
     'IfTrueTransformer': 0,
     'RandomParameterNameTransformer': 0}
    """
    results = {}
    for trans in transformers:
        pattern = trans
        results[trans] = len(re.findall(pattern, genotype))
    return results


def extract_genotype_from_json(genotype: [dict], transformers: [str] = known_transformers) -> dict:
    """
    Tries to count the given Transformers in the given Genotype-Str.
    Result is a dictionary with the count of each Transformer.

    :param genotype: the transformers of one datapoint, a list of transformer+seed objects
    :param transformers: a list of all available transformers to check for
    :return: a dictionary with each transformers count

    >>> example_genotype_json = [{"transformer": "RenameVariableTransformer", "seed": 1131100509 },{ "transformer": "AddNeutralElementTransformer", "seed": -1887344816 },{"transformer": "IfFalseElseTransformer", "seed": -1554943859 },{"transformer": "LambdaIdentityTransformer", "seed": 2097957312 },{"transformer": "AddUnusedVariableTransformer", "seed": 2076014978}]
    >>> transformers = [ "RenameVariableTransformer","IfFalseElseTransformer","AddNeutralElementTransformer","AddUnusedVariableTransformer","LambdaIdentityTransformer","IfTrueTransformer", "RandomParameterNameTransformer"]
    >>> extract_genotype_from_json(example_genotype_json,transformers=transformers)
    {'RenameVariableTransformer': 1,
     'IfFalseElseTransformer': 1,
     'AddNeutralElementTransformer': 1,
     'AddUnusedVariableTransformer': 1,
     'LambdaIdentityTransformer': 1,
     'IfTrueTransformer': 0,
     'RandomParameterNameTransformer': 0}

    """
    result = {}
    found_trans = [g["transformer"] for g in genotype]
    for trans in transformers:
        result[trans]=found_trans.count(trans)
    return result


if __name__ == "__main__":
    print(f"Starting to extract data from {sys.argv[1]}")
    make_csv(path_to_data_dir=sys.argv[1])
    print(f"Finished, closing the program")
