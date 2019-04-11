define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'voc_user_form',

                record: data.record,

                fields : [                                
                    {name: 'surname', type: 'text'},
                    {name: 'firstname', type: 'text'},
                    {name: 'patronymic', type: 'text'},
                    {name: 'is_female', type: 'list', options: {items: [{id:"", text: "Не указано"}, {id:"0", text: "Мужской"}, {id: 1, text: "Женский"}]}},
                ],

            })

       })

    }

})