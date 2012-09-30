import os

import codecs
import re
import itertools
NUMBER_OF_ADDRESSES = 223


chron_entry_rgx_s = r"\[\[(?P<title>(?P<president_name>.+)'s? .*State of the Union (?:Address|Speech))\|(?P<address_number>\w+) State of the Union Address\]\] - \[\[author:(?P<author_name>.+)\|.+\]\], \((?P<day>\d+) (?P<month>\w+) \[\[w:(?P<year>\d+)\|(?P=year)\]\]\)"
chron_entry_rgx = re.compile(chron_entry_rgx_s, re.I)
nums = {'First':'1', 'Second':'2', 'Third':'3', 'Fourth':'4', 'Fifth':'5', 'Sixth':'6', 'Seventh':'7', 'Eighth':'8',
        'Ninth':'9', 'Tenth':'10', 'Eleventh':'11', 'Twelfth':'12'}
def _filename(chron_entry_d):
    prez = chron_entry_d['president_name'].replace(' ','_')
    num = chron_entry_d['address_number']
    return '%s_%s.txt' % (prez, num)

def _extract_metadata(chron_list_filename):
    metadata_text = codecs.open(chron_list_filename,'r','utf-8').read()
    metadata_data = {}
    titles_to_filenames = {}
    for m in chron_entry_rgx.finditer(metadata_text):
        d = m.groupdict()
        d['address_number'] = int(nums[d['address_number']])
        filename = _filename(d)
        titles_to_filenames[d['title']] = filename
        metadata_data[filename] = d
    return metadata_data, titles_to_filenames


def _docs_iterator(chron_list_filename, addresses_filename):
    print "extract_state_of_the_union({0},{1})".format(chron_list_filename, addresses_filename)
    _metadata_data, titles_to_filenames = _extract_metadata(chron_list_filename)
    
    print 'Addresses in index: ' + str(len(titles_to_filenames))
    count = 0
    title = None
    lines = []
    with codecs.open(addresses_filename, 'r', 'utf-8') as r:
        for line in r:
            line = line.strip()
            if line in titles_to_filenames:
                if title is not None:
                    filename = titles_to_filenames[title]
                    yield filename, title, lines
                    count += 1
                    lines = []
                title = line
            else:
                lines += [line]
    filename = titles_to_filenames[title]
    yield filename, title, lines
    count += 1
    
    print 'Addresses extracted: ' + str(count)
    if count < len(titles_to_filenames): raise Exception('Some addresses were not extracted')


def _grouper(n, iterable, fillvalue=None):
    "Collect data into fixed-length chunks or blocks"
    # grouper(3, 'ABCDEFG', 'x') --> ABC DEF Gxx
    args = [iter(iterable)] * n
    return itertools.izip_longest(fillvalue=fillvalue, *args)

def _chunk_iterator(chron_list_filename, addresses_filename, chunk_size=2, skip_last=2):
    for filename, title, lines in _docs_iterator(chron_list_filename, addresses_filename):
        for chunk_num, chunk in enumerate(_grouper(chunk_size, lines[:-skip_last])):
            chunk_filename = '%s:%i.txt' % (filename.replace('.txt',''), chunk_num)
            chunk_title = '%s (%i)' % (title, chunk_num)
            yield chunk_filename, chunk_title, chunk

def chunk(chron_list_filename, addresses_filename, dest_dir):
    for chunk_filename, chunk_title, chunk in _chunk_iterator(chron_list_filename, addresses_filename):
        print(chunk_filename)
        print(chunk_title)
        
        for line in chunk:
            if line is not None:
                print('\t' + line)
        print
        
        with codecs.open(dest_dir+'/'+chunk_filename, 'w', 'utf-8') as w:
            for line in chunk:
                if line is not None:
                    w.write(line)
                    w.write('\n\n')
                



if __name__=='__main__':
    data_dir = os.getenv('HOME') + '/Projects/topicalguide/raw-data/state_of_the_union'
    chron_list_filename = '%s/chronological_list.wiki' % data_dir
    addresses_filename = '%s/state_of_the_union_addresses.txt' % data_dir
    dest_dir = os.getenv('HOME') + '/Projects/Output/Mallet/datasets/sotu_chunks'
    chunk(chron_list_filename, addresses_filename, dest_dir)
