define ([], function () {

    return function (data, view) {
        
        var name = 'voting_protocol_vote_initiators_new_org_form'

        var grid = w2ui ['voting_protocol_vote_initiators_grid']

        var ids = []
        grid.records.forEach ((element, i, arr) => {if (element['uuid_org']) ids.push(element['uuid_org'])})

        $(view).w2popup('open', {

            width  : 500,
            height : 150,

            title   : 'Добавление инициатора - организации',

            onOpen: function (event) {

                event.onComplete = function () {

                    var name = 'voting_protocol_vote_initiators_new_org_form'

                    if (w2ui [name]) w2ui [name].destroy ()

                    $('#w2ui-popup .w2ui-form').w2form ({

                        name: name,

                        fields : [
                            {name: 'uuid_org', type: 'list', options: 
                                {
                                    url: '/mosgis/_rest/?type=voc_organizations&part=list',
                                    postData: {'ids_off': ids},
                                    cachMax: 10,

                                    onLoad: function (e) {

                                        if (e.xhr.status != 200) return $_DO.apologize ({jqXHR: e.xhr})

                                        var content = JSON.parse (e.xhr.responseText).content
                                        var data = { status : "success" }

                                        delete content.cnt
                                        delete content.portion
                                        delete content.total

                                        for (key in content) {
                                            var key_content = []
                                            content [key].forEach ((element, i, array) => {
                                                key_content.push ({'id': element['id'], 'text': element['text']})
                                            })
                                            data.records = key_content
                                            e.xhr.responseText = JSON.stringify (data)
                                            e.xhr.responseJSON = data
                                            e.data = e.xhr.responseJSON
                                        }
                                        
                                    }
                                }
                            },
                        ],

                    });

                    clickOn ($('#w2ui-popup button'), $_DO.update_voting_protocol_vote_initiators_new_org)

                }
                
            }
            
        });
    
    }
    
    
});