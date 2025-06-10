
$(document).ready(function () {
  Chart.register(ChartDataLabels);
  console.log("jQuery document ready 실행됨");

  // 오늘 로그인 수 가져오기
  fetch('/admin/api/dashboard/today-login-count')
    .then(res => res.json())
    .then(data => {
      console.log("오늘 로그인 수 데이터:", data);
      $('#todayLoginCount').text(data.todayLoginCount + '명');
    })
    .catch(err => console.error("API 호출 실패:", err));

  // 일주일간 로그인 통계 차트
  fetch('/admin/api/dashboard/weekly-login-stats')
    .then(res => res.json())
    .then(data => {
      console.log("일주일 로그인 통계:", data);

      const ctx = document.getElementById('weeklyLoginChart').getContext('2d');

      new Chart(ctx, {
        type: 'line',
        data: {
          labels: data.labels, // ['5/24', '5/25', ..., '5/30']
          datasets: [{
            label: '일일 로그인 수',
            data: data.counts, // [3, 4, 5, 2, ...]
            borderColor: 'rgba(54, 162, 235, 1)',
            backgroundColor: 'rgba(54, 162, 235, 0.2)',
            borderWidth: 2,
            fill: true,
            tension: 0.3
          }]
        },
        options: {
          responsive: true,
          scales: {
            y: {
              beginAtZero: true,
              ticks: {
                stepSize: 1
              }
            }
          },
          plugins: {
            legend: {
              display: true,
              position: 'top'
            }
          }
        }
      });
    })
    .catch(err => console.error("주간 로그인 통계 API 호출 실패:", err));
	
	// 가입자 수 통계
	fetch('/admin/statistics/weekly-join')
	  .then(res => res.json())
	  .then(data => {
	    console.log("주간 가입자 통계:", data);

	    const weeklyStats = data.weeklyStats;
	    const totalCount = data.totalCount;

	    // 전체 가입자 수 표시 (선택)
	    $('#totalJoinCount').text(totalCount + '명');

	    // 차트 데이터 준비
	    const labels = weeklyStats.map(s => s.week); // ex: ["2025-22", "2025-23", ...]
	    const counts = weeklyStats.map(s => s.count); // ex: [5, 8, 12, ...]

	    // 가입자 차트 그리기
	    const ctx = document.getElementById('userChart').getContext('2d');
	    new Chart(ctx, {
	      type: 'bar',
	      data: {
	        labels: labels,
	        datasets: [{
	          label: '주간 가입자 수',
	          data: counts,
	          backgroundColor: 'rgba(255, 99, 132, 0.6)',
	          borderColor: 'rgba(255, 99, 132, 1)',
	          borderWidth: 1
	        }]
	      },
	      options: {
	        responsive: true,
	        scales: {
	          y: {
	            beginAtZero: true,
	            ticks: {
	              stepSize: 1
	            }
	          }
	        },
	        plugins: {
	          legend: {
	            display: true,
	            position: 'top'
	          }
	        }
	      }
	    });
	  })
	  .catch(err => console.error("주간 가입자 통계 API 호출 실패:", err));
	  
	  
	  // 1달간 상품 등록 통계
	  fetch('/admin/statistics/weekly-product')
	    .then(res => res.json())
	    .then(data => {
	      const productCtx = document.getElementById('productChart').getContext('2d');
	      const labels = data.weeklyStats.map(item => item.week);
	      const counts = data.weeklyStats.map(item => item.count);

	      new Chart(productCtx, {
	        type: 'bar',
	        data: {
	          labels: labels,
	          datasets: [{
	            label: '주간 상품 등록 수',
	            data: counts,
	            backgroundColor: 'rgba(255, 206, 86, 0.6)',
	            borderColor: 'rgba(255, 206, 86, 1)',
	            borderWidth: 1
	          }]
	        },
	        options: {
	          responsive: true,
	          scales: {
	            y: {
	              beginAtZero: true,
	              ticks: {
	                stepSize: 1
	              }
	            }
	          }
	        }
	      });
	    })
	    .catch(err => console.error("주간 상품 등록 통계 API 실패:", err));
		
		
		// 1달 전체 상품 수 표시
	  fetch('/admin/statistics/total-products')
		.then(res => res.json())
		.then(data => {
		  document.getElementById('totalProductCount').textContent = data.totalProductCount + '개';
		})
		.catch(err => console.error("전체 상품 수 API 호출 실패:", err));
		
		// 3. 카테고리별 상품 수 (퍼센트 표시)
		fetch('/admin/statistics/category-product-stats')
		  .then(res => res.json())
		  .then(data => {
		    const ctx = document.getElementById('categoryChart');
		    const labels = data.map(d => d.categoryName);
		    const counts = data.map(d => d.count);
		    const total = counts.reduce((a, b) => a + b, 0);

		    new Chart(ctx, {
		      type: 'doughnut',
		      data: {
		        labels: labels,
		        datasets: [{
		          data: counts,
		          backgroundColor: ['#ff6384', '#36a2eb', '#ffcd56', '#4bc0c0', '#9966ff', '#ff9f40']
		        }]
		      },
		      options: {
		        responsive: true,
		        plugins: {
		          legend: {
		            position: 'bottom'
		          },
		          tooltip: {
		            callbacks: {
		              label: function(context) {
		                const value = context.parsed;
		                const percent = total > 0 ? ((value / total) * 100).toFixed(1) : 0;
		                return `${context.label}: ${value}개 (${percent}%)`;
		              }
		            }
		          },
		          datalabels: {
		            color: '#fff',
		            formatter: (value, ctx) => {
		              const percent = total > 0 ? ((value / total) * 100).toFixed(1) : 0;
		              return `${percent}%`;
		            },
		            font: {
		              weight: 'bold',
		              size: 14
		            }
		          }
		        }
		      },
		      plugins: [ChartDataLabels]
		    });
		  })
		  .catch(err => console.error("카테고리별 상품 수 API 실패:", err));
		
		
		// 거래 성사율 파이 차트
		fetch('/admin/api/dashboard/trade-stats')
		  .then(res => res.json())
		  .then(data => {
			console.log("거래 성사율 데이터:", data); // 여기 추가
		    const ctx = document.getElementById('tradePieChart').getContext('2d');
		    
			new Chart(ctx, {
			  type: 'pie',
			  data: {
			    labels: ['거래 성사', '거래 미성사'],
			    datasets: [{
			      label: '거래 성사율',
			      data: [data['성사'], data['미성사']],
			      backgroundColor: ['#4CAF50', '#F44336'],
			      borderColor: '#fff',
			      borderWidth: 2,
			      hoverOffset: 20, // 마우스 올릴 때 살짝 튀어나오는 효과
			    }]
			  },
			  options: {
			    responsive: true,
			    plugins: {
			      legend: {
			        position: 'bottom',
			        labels: {
			          font: { size: 14 },
			          padding: 20,
			        }
			      },
			      tooltip: {
			        callbacks: {
			          label: function(context) {
			            const total = data['성사'] + data['미성사'];
			            const value = context.parsed;
			            const percent = total > 0 ? ((value / total) * 100).toFixed(1) : 0;
			            return `${context.label}: ${value}건 (${percent}%)`;
			          }
			        }
			      },
			      datalabels: {
			        color: '#fff',
			        formatter: (value, ctx) => {
			          const total = ctx.chart.data.datasets[0].data.reduce((a,b) => a + b, 0);
			          const percent = total > 0 ? (value / total * 100).toFixed(1) : 0;
			          return `${percent}%`;
			        },
			        font: {
			          weight: 'bold',
			          size: 16,
			        }
			      }
			    }
			  },
			  plugins: [ChartDataLabels] // datalabels 플러그인 필요
			});
			
			
			/*new Chart(ctx, {
		      type: 'pie',
		      data: {
		        labels: ['거래 성사', '거래 미성사'],
		        datasets: [{
		          label: '거래 성사율',
		          data: [data['성사'], data['미성사']],
		          backgroundColor: ['#4CAF50', '#F44336'],
		        }]
		      }
		    });*/
			const total = data['성사'] + data['미성사'];
			  const rate = total > 0 ? ((data['성사'] / total) * 100).toFixed(1) : 0;
			  document.getElementById('tradeSummary').textContent = 
			    `전체 거래 ${total}건 중 ${data['성사']}건 성사 (성사율: ${rate}%)`;
			
			
		  })
		  .catch(err => console.error("거래 성사율 API 호출 실패:", err));
	
});