define ([], function () {

    return function (data, view) {
        
        var name = 'voting_protocol_vote_initiators_new_org_form'

        var grid = w2ui ['voting_protocol_vote_initiators_grid']

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
                                    postData: {'protocol_uuid': data.item.uuid},
                                    cacheMax: 10,

                                    onLoad: function (e) {

                                        if (e.xhr.status != 200) return $_DO.apologize ({jqXHR: e.xhr})

                                        var content = JSON.parse (e.xhr.responseText).content
                                        var data = { status : "success", total : content.cnt }

                                        delete content.cnt
                                        delete content.portion
                                        delete content.total

                                        for (key in content) {
                                            data.records = dia2w2uiRecords (content [key])
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