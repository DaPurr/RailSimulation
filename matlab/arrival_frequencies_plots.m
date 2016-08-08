% 10 min interval
data_20160112 = csvread('../data/cico/export/20160112_rta.csv');
%data_rta_ut_20160112 = csvread('../data/cico/frequencies/20160112/freqs_rta_ut_10min.csv');
%data_rta_rtd_20160112 = csvread('../data/cico/frequencies/20160112/freqs_rta_rtd_10min.csv');
%data_rta_gd_20160112 = csvread('../data/cico/frequencies/20160112/freqs_rta_gd_10min.csv');

data_20160209 = csvread('../data/cico/export/20160209_rta.csv');
% data_rta_ut_20160209 = csvread('../data/cico/rta_ut_20160209.csv');
%data_rta_rtd_20160209 = csvread('../data/cico/rta_rtd_20160209.csv');
%data_rta_gd_20160209 = csvread('../data/cico/frequencies/20160209/freqs_rta_gd_10min.csv');

data_20160315 = csvread('../data/cico/export/20160315_rta.csv');
%data_rta_ut_20160315 = csvread('../data/cico/frequencies/20160315/freqs_rta_ut_10min.csv');
%data_rta_rtd_20160315 = csvread('../data/cico/frequencies/20160315/freqs_rta_rtd_10min.csv');
%data_rta_gd_20160315 = csvread('../data/cico/frequencies/20160315/freqs_rta_gd_10min.csv');

%arrival_rates_rta_20160209 = csvread('../data/cico/rates_piecewise_constant.csv');

% 1 min interval
% data_rta_rtd_hf = csvread('../data/cico/frequencies/freqs_rta_rtd_1min.csv');

%x = 1:length(data_rta_20160209);
%x_hf = 1:length(data_rta_rtd_hf);
figure;
hold on
plot(data_20160112(:,1), data_20160112(:,2), 'Color', 'red');
xlabel('t');
ylabel('arrivals/t');
figure;
plot(data_20160209(:,1), data_20160209(:,2), 'Color', 'blue');
xlabel('t');
ylabel('arrivals/t');
figure;
plot(data_20160315(:,1), data_20160315(:,2), 'Color', 'magenta');
xlabel('t');
ylabel('arrivals/t');
hold off
%hold on;
%plot(data_rta_20160209(:,1), data_rta_20160209(:,2), 'Color', 'red')
%hold off;