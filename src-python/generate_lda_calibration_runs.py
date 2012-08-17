import os

def generate(dataset, topic_counts, num_runs, output_dir):
    for num_topics in topic_counts:
        for run in range(num_runs):
            print("K=%i #%i" % (num_topics,run))
            name = 'lda%itopics_%i' % (num_topics, run)
            cmd = '''~/Projects/External/mallet-2.0.7/bin/mallet train-topics --input ~/Projects/Output/Mallet/datasets/{dataset}.mallet --output-topic-keys {output_dir}/{name}.keys --output-state {output_dir}/{name}.state --output-doc-topics {output_dir}/{name}.doctopics --doc-topics-max 10 --num-topics {num_topics} --optimize-interval 10 --num-iterations 500'''.format(dataset=dataset,name=name,output_dir=output_dir,num_topics=num_topics)
            print(cmd)
            os.system(cmd)

if __name__=='__main__':
#    dataset = "reuters21578_noblah"
    dataset = "state_of_the_union"
    topic_counts = (10,20,50,100,200)
    num_runs = 5
    output_dir = '~/Projects/Output/EDAValidation/topic_count_calibration/%s' % dataset
    generate(dataset, topic_counts, num_runs, output_dir)
