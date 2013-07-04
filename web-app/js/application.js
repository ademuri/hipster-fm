if (typeof jQuery !== 'undefined') {
	(function($) {
		$('#spinner').ajaxStart(function() {
			$(this).fadeIn();
		}).ajaxStop(function() {
			$(this).fadeOut();
		});
	})(jQuery);
}


function getCache(cacheName, link, callback) {
	var cache = store.get(cacheName);
	
	if (cache && new Date().getDate() - new Date(cache.date).getDate() < 2) {
		callback(cache.data);
	}
	else {
		$.ajax(link, {
			cache: true,
			success: function(data, textStatus, jqXHR) {
				var cache = new Object();
				cache.data = data;
				cache.date = new Date();
				store.set(cacheName, cache);
				callback(data);
			}
		});
	}
}