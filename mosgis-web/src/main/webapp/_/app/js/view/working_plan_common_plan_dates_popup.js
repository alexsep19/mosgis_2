define ([], function () {

    return function (data, view) {
    
        var name = 'working_plan_common_plan_dates_popup_form'
        
        $(fill (view, data)).w2popup ('open', {

            width  : 280,
            height : 310,

            title   : data.label + ', ' + w2utils.settings.fullmonths [data.month] + ' ' + data.year,

            onOpen: function (event) {

                event.onComplete = function () {

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2reform ({
                        name: name,
                        record: data,
                        fields : [],                        
                        onRefresh: function () {                       
                            clickOn ($('table.cal td.local'), $_DO.toggle_working_plan_common_plan_dates_popup)
                        }                        
                    });

                }

            }

        });

    }

});