% 10 min interval
%data_rta_20160112 = csvread('../data/cico/frequencies/20160112/freqs_rta_10min.csv');
%data_rta_ut_20160112 = csvread('../data/cico/frequencies/20160112/freqs_rta_ut_10min.csv');
%data_rta_rtd_20160112 = csvread('../data/cico/frequencies/20160112/freqs_rta_rtd_10min.csv');
%data_rta_gd_20160112 = csvread('../data/cico/frequencies/20160112/freqs_rta_gd_10min.csv');

%data_rta_20160209 = csvread('../data/cico/frequencies/20160209/freqs_rta_10min.csv');
%data_rta_ut_20160209 = csvread('../data/cico/frequencies/20160209/freqs_rta_ut_10min.csv');
data_rta_rtd_20160209 = csvread('../data/cico/rta_rtd_20160209.csv');
%data_rta_gd_20160209 = csvread('../data/cico/frequencies/20160209/freqs_rta_gd_10min.csv');

%data_rta_20160315 = csvread('../data/cico/frequencies/20160315/freqs_rta_10min.csv');
%data_rta_ut_20160315 = csvread('../data/cico/frequencies/20160315/freqs_rta_ut_10min.csv');
%data_rta_rtd_20160315 = csvread('../data/cico/frequencies/20160315/freqs_rta_rtd_10min.csv');
%data_rta_gd_20160315 = csvread('../data/cico/frequencies/20160315/freqs_rta_gd_10min.csv');

%arrival_rates_rta_20160209 = csvread('../data/cico/rates_piecewise_constant.csv');

% 1 min interval
% data_rta_rtd_hf = csvread('../data/cico/frequencies/freqs_rta_rtd_1min.csv');

%x = 1:length(data_rta_20160209);
%x_hf = 1:length(data_rta_rtd_hf);
figure;
plot(data_rta_rtd_20160209(:,1), data_rta_rtd_20160209(:,2));
%hold on
%plot(arrival_rates_rta_20160209(:,1), arrival_rates_rta_20160209(:,2), 'color', 'red');
%hold off