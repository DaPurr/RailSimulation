matrix_2016_01 = dlmread('../data/midt/CQM Fracties 2016-01.csv', ';', 1, 5);
matrix_2016_02 = dlmread('../data/midt/CQM Fracties 2016-02.csv', ';', 1, 5);
matrix_2016_03 = dlmread('../data/midt/CQM Fracties 2016-03.csv', ';', 1, 5);

% sort based on product ID's
matrix_2016_01 = sortrows(matrix_2016_01);
product_codes1 = unique(matrix_2016_01(:,1));
matrix_2016_02 = sortrows(matrix_2016_02);
product_codes2 = unique(matrix_2016_02(:,1));
matrix_2016_03 = sortrows(matrix_2016_03);
product_codes3 = unique(matrix_2016_03(:,1));

mean1 = accumarray(matrix_2016_01(:,1), matrix_2016_01(:,2), [], @mean);
mean2 = accumarray(matrix_2016_02(:,1), matrix_2016_02(:,2), [], @mean);
mean3 = accumarray(matrix_2016_03(:,1), matrix_2016_03(:,2), [], @mean);
mean1 = [product_codes1 mean1];
mean2 = [product_codes2 mean2];
mean3 = [product_codes3 mean3];
overview_mean = [mean1(:,2) mean2(:,2) mean3(:,2)]

std1 = accumarray(matrix_2016_01(:,1), matrix_2016_01(:,2), [], @std);
std2 = accumarray(matrix_2016_02(:,1), matrix_2016_02(:,2), [], @std);
std3 = accumarray(matrix_2016_03(:,1), matrix_2016_03(:,2), [], @std);
std1 = [product_codes1 std1];
std2 = [product_codes2 std2];
std3 = [product_codes3 std3];
overview_std = [std1(:,2) std2(:,2) std3(:,2)]