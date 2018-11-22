define ([], function () {
    
    var grid_name = 'voting_protocol_vote_decision_lists_grid'
    
    function getData () {
        return $('body').data ('data')
    }
    
    return function (data, view) {

        function show () {
            if (data.item.house_uuid && (data.item.id_prtcl_status == 10 || data.item.id_prtcl_status == 11))
                return true
            return false
        }

        var layout = w2ui ['topmost_layout']
        
        var $panel = $(layout.el ('main'))

        $panel.w2regrid ({

            multiSelect: false,

            name: grid_name,

            show: {
                toolbar: true,
                footer: true,
                toolbarColumns: true,
                toolbarAdd: show (),
                toolbarEdit: show (),
                toolbarDelete: show (),
            },            

            textSearch: 'contains',

            searches: [            
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}},
            ].filter (not_off),

            columns: 
            [                
                {field: 'questionnumber', caption: 'Номер', size: 10},
                {field: 'questionname', caption: 'Вопрос', size: 10},
                {field: 'agree', caption: 'За', size: 7},
                {field: 'against', caption: 'Против', size: 7},
                {field: 'abstent', caption: 'Воздержались', size: 7},
                {field: 'total', caption: 'Всего', size: 7},
                {field: 'votingresume', caption: 'Итоги голосования', size: 10, 
                        render: function (r) {return r.meetingeligibility == "M" ? 'Решение принято' : 'Решение не принято'}
                },
                {field: 'decisiontype_vc_nsi_63',  caption: 'Тип вопроса',  type: 'enum', options: {items: data.vc_nsi_63.items}},
            ].filter (not_off),

            postData: {data: {"protocol_uuid": data.item.uuid, "house_uuid": data.item.house_uuid}},

            url: '/mosgis/_rest/?type=vote_decision_lists',
           
            onDelete: $_DO.delete_voting_protocol_vote_decision_lists,

            onDblClick: function (e) {
                
                openTab ('/vote_decision_lists/' + e.recid)
            
            },
            
        })

    }
    
})