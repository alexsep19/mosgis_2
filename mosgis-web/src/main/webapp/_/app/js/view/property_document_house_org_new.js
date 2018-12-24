define ([], function () {

    var form_name = 'property_document_house_org_new_form'

    return function (data, view) {

        $(fill (view, data.record)).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: form_name,

                record: data.record,

                fields : [
                    {name: 'uuid_house', type: 'list', hint: 'Адрес', options: {
                        url: '/mosgis/_rest/?type=houses',
                        filter: false,
                        cacheMax: 50,
                        postData: {offset: 0, limit: 50},
                        onLoad: function (e) {
                            e.data = {
                                status: "success",
                                records: e.data.content.tb_houses.map(function (i) {
                                    return {
                                        id: i.id,
                                        text: (i.is_condo ? 'МКД' : 'ЖД') + ' ' + i.address
                                    }
                                })
                            }
                        }
                    }},
                    {name: 'no', type: 'text'},
                    {name: 'dt', type: 'date'},
                    {name: 'prc', type: 'float', options: {min: 0, max: 100}},
                    {name: 'id_type', type: 'list', options: {items: data.vc_prop_doc_types.items}},
                    {name: 'uuid_premise', type: 'list', options: {items: data.premises}},
                    {name: 'uuid_org_owner', type: 'list', options: {items: data.vc_orgs}},
                ],

                focus: 1,

                onChange: function (e) {

                    var form = this

                    if (e.target == 'uuid_house' ) {

                        var uuid_house = e.value_new.id

                        e.done(function(){
                            query({type: 'premises', id: undefined}, {data: {uuid_house: uuid_house || '00'}}, function (d) {

                                var f = form.get('uuid_premise')

                                f.options.items = d.vw_premises.map(function (i) {
                                    return {
                                        id: i.id,
                                        text: i.label
                                    }
                                })

                                delete form.record.uuid_premise

                                $().w2overlay(); // HACK: lost focus, hide dropdown on Enter

                                form.refresh()
                            })
                        })
                    }
                },

                onRefresh: function(e) {

                    e.done (function () {

                        var uuid_house = this.record.uuid_house

                        this.fields.map(function(i){

                            var ro = i.name == 'uuid_house'? false
                                : (i.name == 'uuid_org_owner' || (uuid_house? false: true))

                            i.$el.prop('readonly', ro)
                        })
                    })
                }

            })

       })

    }

})