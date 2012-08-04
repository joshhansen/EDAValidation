'''Computes Fleiss' kappa for Mechanical Turk labelings'''

import csv
import os
import sys
from collections import Counter, defaultdict

def _print_counts(counts, task_ids, labels):
    # Header
    for label in labels:
        sys.stdout.write('\t')
        sys.stdout.write(label)
    sys.stdout.write('\n')
    
    for task_id in task_ids:
        sys.stdout.write(task_id)
        for label in labels:
            sys.stdout.write('\t')
            sys.stdout.write(str(counts[task_id][label]))
        sys.stdout.write('\n')

def fleiss_kappa(data, task_col, rater_col, label_col, label_label_cols):
    task_col_num = data[0].index(task_col)
    rater_col_num = data[0].index(rater_col)
    label_col_num = data[0].index(label_col)
    label_label_col_nums = [ data[0].index(llc) for llc in label_label_cols ]
    
    task_ids = set()
    labels = set()
    task_label_counts = defaultdict(Counter)
    
    for datum in data[1:]:
        task_id = datum[task_col_num]
#        rater_id = datum[rater_col_num]
#        label = datum[label_col_num]
        label = datum[label_label_col_nums[ int(datum[label_col_num]) - 1 ]]
        
        task_ids.add(task_id)
        labels.add(label)
        task_label_counts[task_id][label] += 1
    
    task_ids = sorted(task_ids)
    labels = sorted(labels)
    
    _print_counts(task_label_counts, task_ids, labels)
    
    N = len(task_ids)
    n = sum(task_label_counts[task_ids[0]].values()) # Assumes all tasks are rated equal number of times
    
    
    # P(label)
    def p_j(counts, label):
        total = 0
        label_freq = 0
        for counter in counts.values():
            total += sum(counter.values())
            label_freq += counter[label]
        return float(label_freq) / total
    
    def pbar_e(counts):
        sum = 0.0
        
        for label in labels:
            sum += p_j(counts, label)**2
        
        return sum
    
    def pbar(counts):
        normalizer = 1.0 / (N * n * (n-1))
        
        sum = 0.0
        
        for task_id in task_ids:
            for label in labels:
                sum += counts[task_id][label]**2
        sum -= N*n
        
        return normalizer * sum
    
    pb_e = pbar_e(task_label_counts)
    
    return (pbar(task_label_counts) - pb_e) / (1 - pb_e)
    
    

if __name__=="__main__":
    filename = os.environ['HOME'] + '/Thesis/analysis/Batch_840229_batch_results.csv'
    with open(filename) as file_r:
        csv_r = csv.reader(file_r, delimiter=",", quotechar='"')
        data = [x for x in csv_r]
    kappa = fleiss_kappa(data, "HITId", "WorkerId", "Answer.result", ["Input.model1","Input.model2"])
    print("Fleiss' kappa: %s" % kappa)
        
