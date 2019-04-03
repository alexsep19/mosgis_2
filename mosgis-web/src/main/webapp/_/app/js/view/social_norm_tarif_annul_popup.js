define ([], function () {

    return function (data, view) {
    
        var name = 'social_norm_tarif_annul_popup_form'
        
        $(view).w2popup('open', {

            width  : 580,
            height : 240,

            title   : 'Аннулирование Социальная норма потребления ЭЭ',

            onOpen: function (event) {

                event.onComplete = function () {

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [
                            {name: 'cancelreason',  type: 'text'},
                        ],
                                                
                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_social_norm_tarif_annul_popup)

                }

            }

        });

    }

});