define ([], function () {

    return function (data, view) {
    
        var name = 'working_list_common_plan_dates_popup_form'
        
        $(view).w2popup ('open', {

            width  : 280,
            height : 310,

            title   : data.label + ', ' + w2utils.settings.fullmonths [data.month] + ' ' + data.year,

            onOpen: function (event) {

                event.onComplete = function () {

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({
                        name: name,
                        fields : [],
                        
                        onRefresh: function () {
                        
                            var $cal = $('table.cal')                      
                            
                            var dt = new Date (data.year, data.month, 1)
                            dt.setDate (dt.getDate () - (dt.getDay () + 6) % 7)

                            for (var i = 0; i < 5; i ++) {

                                var $tr = $('<tr>').appendTo ($cal)

                                for (var j = 0; j < 7; j ++) {

                                    var $td = $('<td>').appendTo ($tr)

                                    $td.text (dt.getDate ())

                                    if (dt.getMonth () == data.month) {                                    
                                        clickOn ($td, $_DO.toggle_working_list_common_plan_dates_popup)                                    
                                    }
                                    else {
                                        $td.addClass ('alien')
                                    }
                                    
                                    dt.setDate (dt.getDate () + 1)

                                }                            

                            }                            
                        
                        }
                        
                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_working_list_common_plan_dates_popup)

                }

            }

        });

    }

});