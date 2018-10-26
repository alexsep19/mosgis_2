define ([], function () {

    return function (data, view) {
    
        var name = 'charter_terminate_popup_form'
        
        var it = data.item
        
        $(view).w2popup('open', {

            width  : 545,
            height : 200,

            title   : 'Прекращение действия устава',

            onOpen: function (event) {

                event.onComplete = function () {

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,
                        
                        record: {
                            reason: it.reason,
                            terminate: dt_dmy (it.terminate),
                        },

                        fields : [
                            {name: 'reason',  type: 'text' },
                            {name: 'terminate',  type: 'date' },
                        ],
                                                
                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_charter_terminate_popup)

                }

            }

        });

    }

});