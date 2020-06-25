

dataset=human
dataset=mouse
# interactions
wget http://interactome3d.irbbarcelona.org/user_data/$dataset/download/complete/interactions.dat
for c in {0..1}; do for d in {0..9}; do for u in {0..9}; do echo $c$d$u; wget http://interactome3d.irbbarcelona.org/user_data/$dataset/download/complete/interactions_$c$d$u.tgz; done;done;done
	
# proteins
wget http://interactome3d.irbbarcelona.org/user_data/$dataset/download/complete/proteins.dat
for c in {0..1}; do for d in {0..9}; do for u in {0..9}; do echo $c$d$u; wget http://interactome3d.irbbarcelona.org/user_data/$dataset/download/complete/proteins_$c$d$u.tgz; done;done;done
	
	
dataset=mouse
# interactions
wget http://interactome3d.irbbarcelona.org/user_data/$dataset/download/complete/interactions.dat
for d in {0..1}; do for u in {0..9}; do echo $d$u; wget http://interactome3d.irbbarcelona.org/user_data/$dataset/download/complete/interactions_$d$u.tgz; done;done
	
# proteins
wget http://interactome3d.irbbarcelona.org/user_data/$dataset/download/complete/proteins.dat
for d in {0..9}; do for u in {0..9}; do echo $d$u; wget http://interactome3d.irbbarcelona.org/user_data/$dataset/download/complete/proteins_$d$u.tgz; done;done
