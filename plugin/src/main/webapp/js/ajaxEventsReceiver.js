var AjaxEventsReceiver = (function($) {

	function JenkinsAjaxEventsReceiver() {
		this.lastModifiedTimeStamp = -1;
	}

	JenkinsAjaxEventsReceiver.prototype = {
		start : function() {
			this.handleCurrentBuildTestEvents();
		},

        handleCurrentBuildTestEvents: function() {
			var handler = $.proxy(function(t) {
				var response = t.responseObject();
                console.log('Response returned is: ' + response + ', last modified timestamp is: ' + this.lastModifiedTimeStamp);
                if (this.lastModifiedTimeStamp < response) {
                    if (this.lastModifiedTimeStamp != -1) { //to prevent appearance on first time loading
                        $('#notifications').noty({
                            text: 'You\'ve got a new test result, click to refresh the page.',
                            maxVisible: 1,
                            timeout: 15000,
                            layout: 'topRight',
                            theme: 'relax',
                            type: 'information',
                            callback: {
                                onCloseClick: function() {
                                    $('#dynamic-reports-frame').attr('src', function(i, val){
                                        return val;
                                    });
                                }
                            }
                        });
                    }
                    this.lastModifiedTimeStamp = response;
                }
				// poll every 5 seconds
                setTimeout(callFunction, 5000);
			}, this);

            var callFunction = $.proxy(function() {
				remoteAction.getDynamicReporterLastStamp(handler);
			}, this);

			callFunction();
		}
	};

	return JenkinsAjaxEventsReceiver;
}(jQuery));