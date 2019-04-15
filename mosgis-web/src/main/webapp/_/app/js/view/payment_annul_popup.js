define ([], function () {

    return function (data, view) {
    
        var name = 'payment_annul_popup_form'
        
        $(view).w2popup('open', {

            width  : 580,
            height : 270,

            title   : 'Аннулирование платежа',

            onOpen: function (event) {

                event.onComplete = function () {

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        record: data.record,

                        fields : [
                            {name: 'cancellationdate',  type: 'date'},
                            {name: 'cancellationcomment', type: 'text'},
                        ],

                        focus: 1,
                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_payment_annul_popup)

                }

            }

        });

    }

});