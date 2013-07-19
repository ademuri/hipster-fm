if (typeof jQuery !== 'undefined') {
	(function($) {
		$('#spinner').ajaxStart(function() {
			$(this).fadeIn();
		}).ajaxStop(function() {
			$(this).fadeOut();
		});
	})(jQuery);
}


function getCache(cacheName, spinner, link, callback) {
	var cache = store.get(cacheName);
	if (cache) {
		callback(cache.data);
	}
	
	// get a new list every 60 minutes, may need to bump this up
	if (!cache || (new Date() - new Date(cache.date) > 3600000)) {
		$.ajax(link, {
			cache: true,
			success: function(data, textStatus, jqXHR) {
				var cache = new Object();
				cache.data = data;
				cache.date = new Date();
				store.set(cacheName, cache);
				callback(data);
				
				if (spinner) {
					$(spinner).hide();
				}
			}
		});
	} else if (spinner) {
		$(spinner).hide();
	}
}