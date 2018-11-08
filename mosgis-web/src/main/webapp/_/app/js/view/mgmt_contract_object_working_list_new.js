define ([], function () {

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'working_list_form',

                record: data.record,

                fields : [                                
                    {name: 'dt_from', type: 'list', options: {items: data.begins}},
                    {name: 'dt_to', type: 'list', options: {items: data.ends}},
                ],
                
                focus: -1,

            })

       })

    }

})