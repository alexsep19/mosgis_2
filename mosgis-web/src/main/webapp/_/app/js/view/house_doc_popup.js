define ([], function () {

    return function (data, view) {

        $(view).w2popup ('open', {

            width  : 605,
            height : 280,

            title   : 'Редактирование документа',

            onOpen: function (event) {

                event.onComplete = function () {
                
                    $('#file_label').text (data.label)                    

                    var name = 'house_doc_popup_form'

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        record: data,

                        fields : [
                            {name: 'no',  type: 'text' },
                            {name: 'dt',  type: 'date' },
                            {name: 'note',  type: 'textarea' },
                        ],

                        focus: -1

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_house_doc_popup)
                    clickOn ($('#file_label'), $_DO.download_house_doc_popup)

                }

            }

        })

    }    

});