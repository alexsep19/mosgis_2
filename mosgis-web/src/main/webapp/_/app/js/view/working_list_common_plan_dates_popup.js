define ([], function () {

    return function (data, view) {
    
        var name = 'working_list_common_plan_dates_popup_form'
        
        $(view).w2popup('open', {

            width  : 580,
            height : 240,

            title   : 'zzz',

            onOpen: function (event) {

                event.onComplete = function () {

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [
                            {name: 'reasonofannulment',  type: 'text'},
                        ],
                                                
                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_working_list_common_plan_dates_popup)

                }

            }

        });

    }

});