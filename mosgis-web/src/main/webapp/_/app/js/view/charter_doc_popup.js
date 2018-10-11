define ([], function () {

    return function (data, view) {
    
        $(view).w2popup ('open', {

            width  : 625,
            height : 220,

            title   : 'Редактирование документа',

            onOpen: function (event) {

                event.onComplete = function () {
                                
                    $('#file_label').text (data.label)                    

                    var name = 'charter_doc_popup_form'

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        record: data,

                        fields : [
                            {name: 'description',  type: 'textarea' },
                        ],

                        focus: -1

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_charter_doc_popup)
                    clickOn ($('#file_label'), $_DO.download_charter_doc_popup)                    
                    
                }

            }

        })

    }    

});