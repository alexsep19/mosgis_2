define ([], function () {

    return function (data, view) {
    
        var name = 'settlement_doc_annul_popup_form'
        
        $(view).w2popup('open', {

            width  : 580,
            height : 240,

            title   : 'Аннулирование информации о состоянии расчетов',

            onOpen: function (event) {

                event.onComplete = function () {

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [
                            {name: 'reasonofannulment',  type: 'text'},
                        ],
                                                
                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_settlement_doc_annul_popup)

                }

            }

        });

    }

});