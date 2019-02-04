define ([], function () {

    var form_name = 'infrastructure_net_pieces_popup_form'

    function reset () {

        var data = clone ($('body').data ('data'))
        var record = w2ui[form_name].record

        if (data.item.code_vc_nsi_33 == '4.5') $('#code_vc_nsi_36').prop ('disabled', false)
        else {
            $('#code_vc_nsi_36').val ('')
            $('#code_vc_nsi_36').prop ('disabled', true)
        }
        if (data.item.code_vc_nsi_33 == '5.4') $('#code_vc_nsi_45').prop ('disabled', false)
        else {
            $('#code_vc_nsi_45').val ('')
            $('#code_vc_nsi_45').prop ('disabled', true)
        }

    }

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: form_name,

                record: data.record,

                fields : [
                    {name: 'name', type: 'text'},
                    {name: 'diameter', type: 'text'},
                    {name: 'length', type: 'text'},
                    {name: 'needreplaced', type: 'text'},
                    {name: 'wearout', type: 'text'},
                    {name: 'code_vc_nsi_36', type: 'list', options: {items: data.vc_nsi_36.items}},
                    {name: 'code_vc_nsi_45', type: 'list', options: {items: data.vc_nsi_45.items}},
                ],

                onRefresh: function (e) {

                    e.done (reset)

                }

            })

       })

    }

})