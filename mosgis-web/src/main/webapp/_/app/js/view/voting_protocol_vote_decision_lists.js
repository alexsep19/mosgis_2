define ([], function () {
    
    var grid_name = 'voting_protocol_vote_decision_lists_grid'
    
    function getData () {
        return $('body').data ('data')
    }
    
    return function (data, view) {

        function Permissions () {

            if (!data.item.is_deleted && data.item.house_uuid && (data.item.id_prtcl_status_gis == 10 || data.item.id_prtcl_status_gis == 11)) {
                
                if ($_USER.role.admin) return true

                if (data.cach) {

                    return ($_USER.role.nsi_20_1 ||
                            $_USER.role.nsi_20_19 ||
                            $_USER.role.nsi_20_20 ||
                            $_USER.role.nsi_20_21 ||
                            $_USER.role.nsi_20_22) &&
                            data.cach.is_own &&
                            $_USER.uuid_org == data.cach['org.uuid']

                }

                return $_USER.role.nsi_20_8 && $_USER.role['oktmo_' + data.item['fias.oktmo']]
            }

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
                toolbarAdd: Permissions (),
                toolbarDelete: Permissions (),
            },            

            textSearch: 'contains',

            searches: [            
                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
                    {id: "0", text: "Актуальные"},
                    {id: "1", text: "Удалённые"},
                ]}},
            ].filter (not_off),

            columnGroups:
            [
                {span: 3, caption: ''},
                {span: 4, caption: 'Результаты голосования'},
                {span: 1, caption: '', master: true},
            ],

            columns: 
            [                
                {field: 'questionnumber', caption: 'Номер', size: 2},
                {field: 'questionname', caption: 'Вопрос', size: 13},
                {field: 'decisiontype_vc_nsi_63',  caption: 'Тип вопроса', size: 13, voc: data.vc_nsi_63},
                {field: 'agree', caption: 'За', size: 4},
                {field: 'against', caption: 'Против', size: 4},
                {field: 'abstent', caption: 'Воздержались', size: 4},
                {field: 'total', caption: 'Всего', size: 4},
                {field: 'votingresume', caption: 'Итоги голосования', size: 4, 
                        render: function (r) {return r.votingresume == "M" ? 'Решение принято' : 'Решение не принято'}
                },
            ].filter (not_off),

            postData: {data: {"protocol_uuid": data.item.uuid, "house_uuid": data.item.house_uuid}},

            url: '/mosgis/_rest/?type=vote_decision_lists',
            
            onAdd: $_DO.create_voting_protocol_vote_decision_lists,
            onDelete: $_DO.delete_voting_protocol_vote_decision_lists,

            onDblClick: function (e) {
                
                openTab ('/vote_decision_list/' + e.recid)
            
            },
            
        })

    }
    
})