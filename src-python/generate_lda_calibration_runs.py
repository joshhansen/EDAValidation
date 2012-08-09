import os
for num_topics in (10,20,50,100,200):
    for run in range(5):
        print("K=%i #%i" % (num_topics,run))
        name = 'lda%itopics_%i' % (num_topics, run)
        cmd = '''~/Projects/External/mallet-2.0.7/bin/mallet train-topics --input ~/Projects/Output/LDA/datasets/reuters21578_noblah.mallet --output-topic-keys ./%s.keys --output-state ./%s.state --output-doc-topics ./%s.doctopics --doc-topics-max 10 --num-topics %i --optimize-interval 10 --num-iterations 500''' % (name,name,name,num_topics)
        os.system(cmd)
