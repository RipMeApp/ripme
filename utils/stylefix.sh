echo ""
echo "====================================================="
echo "Tabs are not allowed (please manually fix tabs)"
echo "-----------------------------------------------------"
git grep -n -P "\t" -- :/*.java | sed -e "s/\t/\x1b[7m--->\x1b[m/g"
echo "====================================================="

echo "Removing trailing whitespace..."
git grep -l -P "[ \t]+$" -- :/*.java | xargs -I % sed -i -r -e "s/[ \t]+$//g" %

echo "Replacing '){' with ') {'..."
git grep -l -P "\)\{" -- :/*.java | xargs -I % sed -i -r -e "s/\)\{/) {/g" %

echo "Adding space between keywords and punctuation..."
git grep -l -P "(\b(if|for|while|catch)\b[(])" -- :/*.java | xargs -I % sed -i -r -e "s/(\b(if|for|while|catch)\b[(])/\2 (/g" %
git grep -l -P "(\b(else|do|try|finally)\b[{])" -- :/*.java | xargs -I % sed -i -r -e "s/(\b(else|do|try|finally)\b[{])/\2 {/g" %

